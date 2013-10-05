using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace tman0.JsonAPI2
{
    public class JSONAPI
    {
        public string Host { get; private set; }
        public int Port { get; private set; }
        public string Username { get; set; }
        public string Password { get; set; }
        public string Salt { get; private set; }
        public bool Connected { get; private set; }
        public event EventHandler<StreamDataReceivedEventArgs> StreamDataReceived;

        private bool doInitialize;
        private Thread netReadThread;
        private Dictionary<long, dynamic> data = new Dictionary<long, dynamic>();
        private List<long> streams = new List<long>(); 
        private TcpClient serverConnection = new TcpClient();
        private StreamWriter netOut;
        private StreamReader netIn;

        public JSONAPI(string host, int port, string username, string password, string salt = "")
        {
            Host = host;
            Port = port;
            Username = username;
            Password = password;
            Salt = salt;
            Connected = false;
        }

        public JSONAPI(string host, int port)
        {
            Host = host;
            Port = port;
            Connected = false;
        }

        /// <summary>
        /// Connect to the server.
        /// </summary>
        /// <exception cref="SocketException">An error occurred connecting to the server.</exception>
        public void Connect()
        {
            serverConnection.Connect(Host, Port + 1);
            netOut = new StreamWriter(serverConnection.GetStream());
            netIn = new StreamReader(serverConnection.GetStream());
            netOut.AutoFlush = true;

            // start the inbound data reader stream
            netReadThread = new Thread(readThread);
            netReadThread.Start();

            Connected = true;
        }

        /// <summary>
        /// Connect to the specified server.
        /// </summary>
        /// <param name="host">The hostname of the server to connect to.</param>
        /// <param name="port">The port of the server to connect to.</param>
        public void Connect(string host, int port)
        {
            serverConnection.Connect(host, port + 1);
            netOut = new StreamWriter(serverConnection.GetStream());
            netIn = new StreamReader(serverConnection.GetStream());
            netOut.AutoFlush = true;
            Host = host;
            Port = port;

            // start the inbound data reader stream
            netReadThread = new Thread(readThread);
            netReadThread.Start();

            Connected = true;
        }

        /// <summary>
        /// Generate a key for use with Call().
        /// </summary>
        /// <param name="method">The method to call.</param>
        /// <param name="username">The server's username.</param>
        /// <param name="password">The server's password.</param>
        /// <param name="salt">The server's salt.</param>
        /// <returns></returns>
        public string MakeKey(string method, string username, string password, string salt = "")
        {
            string key = BitConverter.ToString(
                    SHA256.Create().ComputeHash(Encoding.UTF8.GetBytes(username + method + password + salt)))
                    .Replace("-", "").ToLower();
            return key;

        }

        /// <summary>
        /// Call a method on the server. [Thread Safe]
        /// </summary>
        /// <param name="method">The method to call.</param>
        /// <param name="key">The key to use. A key will be generated if left null.</param>
        /// <param name="args">Arguments to pass to the server.</param>
        /// <returns>A string if there was an error, otherwise the object returned by the server.</returns>
        public dynamic Call(string method, string key = null, params object[] args)
        {
            if(!Connected) throw new IOException("Connect to the server first! (Use JsonAPI.Connect())");
            if (key == null) key = MakeKey(method, Username, Password, Salt);   // create a key if there's not already one
            var w = new StringWriter();
            new JsonSerializer().Serialize(w, args);                            
            var now = DateTime.Now.Ticks;                                       // get the current time in ticks so we can refer to it later
            var requestUrl = "/api/call?method=" + method + "&args=" + Uri.EscapeDataString(w.ToString()) + "&key=" + key + "&tag=" + now; // build the request URL
            data.Add(now, null);                                                // let the reader thread know that we're expecting data with this tag
            netOut.WriteLine(requestUrl);                                       // send the request
            while (data[now] == null) ;                                         // hope for the best
            var toReturn = data[now];
            data.Remove(now);
            return toReturn;                                                    // return the rest
        }

        /// <summary>
        /// Call multiple methods on the server.
        /// </summary>
        /// <param name="methods">A string[] of methods to call on the server.</param>
        /// <param name="key">The key to use for this request. A key will be generated if left null.</param>
        /// <param name="args">Arguments to pass to the server.</param>
        /// <returns>A dictionary containing the results of the method calls.</returns>
        public Dictionary<string, dynamic> CallMultiple(string[] methods, string key = null, params object[][] args)
        {
            if (!Connected) throw new IOException("Connect to the server first! (Use JsonAPI.Connect())");
            if (key == null) key = MakeKey(JsonConvert.SerializeObject(methods), Username, Password, Salt);   // create a key if there's not already one
            var w = new StringWriter();
            new JsonSerializer().Serialize(w, args);
            var now = DateTime.Now.Ticks;                                       // get the current time in ticks so we can refer to it later
            var requestUrl = string.Format("/api/call-multiple?method={0}&args={1}&key={2}&tag={3}",
                Uri.EscapeDataString(JsonConvert.SerializeObject(methods)), Uri.EscapeDataString(w.ToString()), key, now); // build the request URL
            data.Add(now, null);                                                // let the reader thread know that we're expecting data with this tag
            netOut.WriteLine(requestUrl);                                       // send the request
            while (data[now] == null) ;                                         // hope for the best
            var toReturn = new Dictionary<string, dynamic>();
            foreach (JObject o in data[now])
            {
                toReturn.Add((string)o["source"], o["success"]);
            }
            data.Remove(now);
            return toReturn;                                                   // return the rest
        }

        /// <summary>
        /// Subscribe to a stream source, which can be viewed with the StreamDataReceived event handler.
        /// </summary>
        /// <param name="source">The source to subscribe to. Generally "console", "chat", or "connections".</param>
        /// <param name="key">The key to use to subscribe. A key will be generated if left null.</param>
        /// <param name="sendPrevious">Whether or not to send the previous 50 items along with the most recent. These will be sent as any other stream message through the StreamDataReceived event.</param>
        public void Subscribe(string source, string key = null, bool sendPrevious = false)
        {
            if (!Connected) throw new IOException("Connect to the server first! (Use JsonAPI.Connect())");
            if (key == null) key = MakeKey(source, Username, Password, Salt);   // create a key if there's not already one
            var now = DateTime.Now.Ticks;
            var requestUrl = string.Format("/api/subscribe?source={0}&key={1}&show_previous={2}&tag={3}", source, key,
                sendPrevious, now);
            streams.Add(now);
            netOut.WriteLine(requestUrl);
        }
        
        /// <summary>
        /// The thread which reads and processes network data.
        /// </summary>
        private void readThread()
        {
            while (!serverConnection.Connected ) ;  // wait for a connection
            while (serverConnection.Connected)
            {
                var line = netIn.ReadLine();        // wait for data
                var response = JsonConvert.DeserializeObject<dynamic>(line);    // deserialize the data we get into a dynamic object
                if (streams.Contains((long) response.tag))
                {
                    if (response.result == "error")    // do more fancy error checking later, an exception here would be nice
                        StreamDataReceived(this, new StreamDataReceivedEventArgs(true, (string)response.source, response.error));
                    else
                        StreamDataReceived(this, new StreamDataReceivedEventArgs(false, (string)response.source, response.success));
                }
                else 
                {
                    if (response.result == "error")    // do more fancy error checking later, an exception here would be nice
                        data[(long) response.tag] = response.error;
                    else
                        data[(long) response.tag] = response.success;
                }

#if JSONAPI_DEBUG
                
#endif
            }
            // We must have disconnected somehow...
            Connected = false;
        }
    }

    public class StreamDataReceivedEventArgs : EventArgs
    {
        public StreamDataReceivedEventArgs(bool error, string source, dynamic data)
        {
            Error = error;
            Data = data;
            Source = source;
        }
        /// <summary>
        /// Whether or not the stream data was the result of an error.
        /// </summary>
        public bool Error { get; private set; }

        /// <summary>
        /// The data returned with the stream message. If Error is true, then it is the error message.
        /// </summary>
        public dynamic Data { get; private set; }

        /// <summary>
        /// The source which the stream message came from.
        /// </summary>
        public string Source { get; private set; }
    }
}

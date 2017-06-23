using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Security.Cryptography;
using System.Text;

namespace JsonApi
{
    public class ApiHelper
    {
        private const string UrlFormat = "http://{0}:{1}/api/2/call?json={2}";

        public ApiHelper(string host, int port, string userName, string password, string salt = "")
        {
            _host = host;
            Port = port;
            _userName = userName;
            _password = password;
            _salt = salt;
        }

        private readonly string _host;
        private readonly string _userName;
        private readonly string _password;
        private readonly string _salt;
        public readonly int Port;

        private string GetKey(string method)
        {
            return BitConverter.ToString(
                    SHA256.Create().ComputeHash(Encoding.UTF8.GetBytes(_userName + method + _password + _salt)))
                .Replace("-", "").ToLower();
        }
        
        private string MakeApiUrl(string method, object[] args)
        {
            return string.Format(UrlFormat, _host, Port, UrlEncode(ConstructCall(method, args)));
        }

        private string MakeApiUrl(string[] method, string[][] args)
        {
            return string.Format(UrlFormat, _host, Port, UrlEncode(ConstructCall(method, args)));
        }
        
        private string ConstructCall(string method, object[] args)
        {
            var json = new
            {
                name = method,
                arguments = args,
                key = GetKey(method),
                username = _userName
            };

            return JsonConvert.SerializeObject(json);
        }
        
        private string ConstructCall(string[] method, string[][] args)
        {
            var allCalls = new List<object>();
            for (var i = 0; i < method.Length; i++)
            {
                allCalls.Add(
                    new
                    {
                        name = method[i],
                        arguments = args[i],
                        key = GetKey(method[i]),
                        username = _userName
                    }
                );
            }

            return JsonConvert.SerializeObject(allCalls);
        }

        /// <summary>
        /// Call a method on the server.
        /// </summary>
        /// <param name="method">The method to call.</param>
        /// <param name="args">Arguments to pass to the server.</param>
        /// <returns></returns>
        public string Call(string method, object[] args)
        {
            var url = MakeApiUrl(method, args);

            return Get(url);
        }

        /// <summary>
        /// Call multiple methods on the server.
        /// </summary>
        /// <param name="method">A string[] of methods to call on the server.</param>
        /// <param name="args">Arguments to pass to the server.</param>
        /// <returns></returns>
        public string Call(string[] method, string[][] args)
        {
            if (method.Length != args.Length)
            {
                throw new ArgumentOutOfRangeException();
            }

            var url = MakeApiUrl(method, args);

            return Get(url);
        }
        
        private static string Get(string url)
        {
            var request = (HttpWebRequest)WebRequest.Create(url);
            request.Method = "GET";
            request.ContentType = "text/html;charset=UTF-8";

            var response = (HttpWebResponse)request.GetResponseAsync().Result;
            var myResponseStream = response.GetResponseStream();
            var myStreamReader = new StreamReader(myResponseStream, Encoding.UTF8);
            var retString = myStreamReader.ReadToEnd();
            myStreamReader.Dispose();
            myResponseStream.Dispose();

            return retString;
        }

        /// <summary>
        /// UrlEncode
        /// </summary>
        /// <param name="str"></param>
        /// <returns></returns>
        private static string UrlEncode(string str)
        {
            var sb = new StringBuilder();
            var byStr = Encoding.UTF8.GetBytes(str);
            for (var i = 0; i < byStr.Length; i++)
            {
                sb.Append(@"%" + Convert.ToString(i, 16));
            }

            return (sb.ToString());
        }
    }
}

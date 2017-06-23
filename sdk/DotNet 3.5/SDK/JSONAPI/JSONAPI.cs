using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;
using System.Security.Cryptography;
using System.IO;
using System.Net;
using JsonExSerializer;

namespace com.ramblingwood.minecraft.jsonapi {
	public class JSONAPI {
		private string host;
		private int port;
		private string username;
		private string password;
		private string salt;
		private Serializer ser = new Serializer(typeof(ArrayList));
		private Serializer deser = new Serializer(typeof(Dictionary<object, object>));

		private string urlFormat_call = "http://{0}:{1}/api/call?method={2}&args={3}&key={4}";
		private string urlFormat_callMultiple = "http://{0}:{1}/api/call-multiple?method={2}&args={3}&key={4}";

		public JSONAPI(string host, int port, string username, string password, string salt) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.salt = salt;
		    ser.Config.OutputTypeInformation = false;
			ser.Config.OutputTypeComment = false;
			ser.Config.IsCompact = true;
		    deser.Config.OutputTypeInformation = false;
			deser.Config.OutputTypeComment = false;
			deser.Config.IsCompact = true;
		}

		public string createKey(IEnumerable methods) {
			string method_json = ToJSON(methods);

			return sha256(username + method_json + password + salt);
		}

		public string createKey(string method) {
			return sha256(username + method + password + salt);
		}

		public Uri makeURL(string method, IEnumerable args) {
			return new Uri(String.Format(urlFormat_call, host, port, Uri.EscapeUriString(method), Uri.EscapeUriString(ToJSON(args)), createKey(method)));
		}

		public Uri makeURLMultiple(IEnumerable methods, IEnumerable args) {
			return new Uri(String.Format(urlFormat_callMultiple, host, port, Uri.EscapeUriString(ToJSON(methods)), Uri.EscapeUriString(ToJSON(args)), createKey(methods)));
		}

		public Dictionary<object, object> call(string method, IEnumerable args) {
			Uri url = makeURL(method, args);

			WebClient c = new WebClient();
			Stream s = c.OpenRead(url);
			StreamReader r = new StreamReader(s);

			return (Dictionary<object, object>)deser.Deserialize(r.ReadToEnd());
		}

		public Dictionary<object,object> call(IEnumerable method, IEnumerable args) {
			return callMultiple(method, args);
		}

		public Dictionary<object, object> callMultiple(IEnumerable methods, IEnumerable args) {
			Uri url = makeURLMultiple(methods, args);

			WebClient c = new WebClient();
			Stream s = c.OpenRead(url);
			StreamReader r = new StreamReader(s);

			return (Dictionary<object,object>)deser.Deserialize(r.ReadToEnd());
		}

		private string sha256(string input) {
			SHA256Managed hash = new SHA256Managed();
			byte[] hashed = hash.ComputeHash(Encoding.UTF8.GetBytes(input));
			return BitConverter.ToString(hashed).Replace("-", "").ToLower();
		}

		public string ToJSON(object obj) {
			return ser.Serialize(obj);
		}
	}
}

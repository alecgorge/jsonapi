using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;
using System.Security.Cryptography;
using System.IO;
using ServiceStack.Text;
using System.Uri;

namespace com.ramblingwood.minecraft.jsonapi {
	public class JSONAPI {
		private string host;
		private int port;
		private string username;
		private string password;
		private string salt;

		private string urlFormat_call = "http://{0}:{1}/api/call?method={2}&args={3}&key={4}";
		private string urlFormat_callMultiple = "http:/{0}:{1}/api/call-multiple?method={2}&args={3}&key={4}";

		public JSONAPI(string host, int port, string username, string password, string salt) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.salt = salt;
		}

		public string createKey(IEnumerable methods) {
			string method_json = ToJSON(methods);

			return sha256(username + method_json + password + salt);
		}

		public string createKey(string method) {
			return sha256(username + method + password + salt);
		}

		public string makeURL(string method, IEnumerable args) {
			return String.Format(urlFormat_call, host, port, Uri.EscapeUriString(method), Uri.EscapeUriString(ToJSON(args)), createKey(method));
		}

		public string makeURLMultiple(IEnumerable methods, IEnumerable args) {
			return String.Format(urlFormat_callMultiple, host, port, Uri.EscapeUriString(ToJSON(methods)), Uri.EscapeUriString(ToJSON(args)), createKey(methods));
		}

		public void call(string method, IEnumerable args) {

		}

		private string sha256(string input) {
			SHA256Managed hash = new SHA256Managed();
			byte[] hashed = hash.ComputeHash(Encoding.UTF8.GetBytes(input));
			return BitConverter.ToString(hashed).Replace("-", "");
		}

		public string ToJSON(object obj) {
			return JsonSerializer.SerializeToString(obj);
		}
	}
}

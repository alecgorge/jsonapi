using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.ramblingwood.minecraft.jsonapi;
using System.Collections;

namespace Sample {
	class Program {
		static void Main(string[] args) {
			JSONAPI j = new JSONAPI("localhost", 20059, "admin", "demo", "");

			ArrayList arr = new ArrayList();
			arr.Add("alefcgorge");
			arr.Add("test");
			Dictionary<object, object> result = j.call("broadcastWithName", arr);
			Console.ReadLine();
			Console.WriteLine(result["result"]);

			if (result[result["result"]] != null && result[result["result"]].ToString() != "null") {
				foreach (object o in ((ArrayList)result[result["result"]])) {
					foreach (object key in ((Hashtable)o).Keys) {
						Console.WriteLine(key + ":" + ((Hashtable)o)[key]);
					}
				}
			}

			Console.ReadLine();
		}
	}
}

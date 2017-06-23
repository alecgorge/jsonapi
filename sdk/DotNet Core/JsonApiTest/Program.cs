using JsonApi;
using System;

namespace JsonApiTest
{
    class Program
    {
        static void Main(string[] args)
        {
            var j = new ApiHelper("192.168.0.197", 25565, "admin", "changeme");
            Console.WriteLine(j.Call("players.name", new object[] { "Tuisku" }));
        }
    }
}
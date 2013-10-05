using System;
using tman0.JsonAPI2;

namespace JsonAPI2Tester
{
    class Program
    {
        static void Main(string[] args)
        {
            var j = new JSONAPI("localhost", 20059, "ftweb", "private");
            j.Connect();
            while (!j.Connected) ;
            j.StreamDataReceived += (sender, eventArgs) => Console.Write(eventArgs.Data["line"]);
            j.Subscribe("console");
            while (true)
            {
                var line = Console.ReadLine();
                j.Call("runConsoleCommand", null, line);
            }
        }
    }
}

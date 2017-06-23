package com.alecgorge.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.java_websocket.util.Base64;

import com.alecgorge.minecraft.jsonapi.util.SlimIOUtils;
import com.alecgorge.minecraft.jsonapi.packets.Lambda;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

/**
 * A simple, tiny, nicely embeddable HTTP 1.0 server in Java
 * 
 * <p>
 * NanoHTTPD version 1.14, Copyright &copy; 2001,2005-2010 Jarno Elonen
 * (elonen@iki.fi, http://iki.fi/elonen/)
 * 
 * <p>
 * <b>Features + limitations: </b>
 * <ul>
 * 
 * <li>Only one Java file</li>
 * <li>Java 1.1 compatible</li>
 * <li>Released as open source, Modified BSD licence</li>
 * <li>No fixed config files, logging, authorization etc. (Implement yourself if
 * you need them.)</li>
 * <li>Supports parameter parsing of GET and POST methods</li>
 * <li>Supports both dynamic content and file serving</li>
 * <li>Never caches anything</li>
 * <li>Doesn't limit bandwidth, request time or simultaneous connections</li>
 * <li>Default code serves files and shows all HTTP parameters and headers</li>
 * <li>File server supports directory listing, index.html and index.htm</li>
 * <li>File server does the 301 redirection trick for directories without '/'</li>
 * <li>File server supports simple skipping for files (continue download)</li>
 * <li>File server uses current directory as a web root</li>
 * <li>File server serves also very long files without memory overhead</li>
 * <li>Contains a built-in list of most common mime types</li>
 * <li>All header names are converted lowercase so they don't vary between
 * browsers/clients</li>
 * 
 * </ul>
 * 
 * <p>
 * <b>Ways to use: </b>
 * <ul>
 * 
 * <li>Run as a standalone app, serves files from current directory and shows
 * requests</li>
 * <li>Subclass serve() and embed to your own program</li>
 * <li>Call serveFile() from serve() with your own base directory</li>
 * 
 * </ul>
 * 
 * See the end of the source file for distribution license (Modified BSD
 * licence)
 */
public class NanoHTTPD {
	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" -> "an example string"
	 */
	public static String decodePercent(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Decodes parameters in percent-encoded URI-format ( e.g.
	 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
	 * Properties. NOTE: this doesn't support multiple identical keys due to the
	 * simplicity of Properties -- if you need multiples, you might want to
	 * replace the Properties with a Hastable of Vectors or such.
	 */
	public static void decodeParms(String parms, Properties p) {
		if (parms == null)
			return;

		StringTokenizer st = new StringTokenizer(parms, "&");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf('=');
			if (sep >= 0)
				p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
		}
	}

	// ==================================================
	// API parts
	// ==================================================

	/**
	 * Override this to customize the server.
	 * <p>
	 * 
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 * 
	 * @parm uri Percent-decoded URI without parameters, for example
	 *       "/index.cgi"
	 * @parm method "GET", "POST" etc.
	 * @parm parms Parsed, percent decoded parameters from URI and, in case of
	 *       POST, data.
	 * @parm header Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public Response serve(String uri, String method, Properties header, Properties parms) {
		System.out.println(method + " '" + uri + "' ");

		Enumeration<?> e = header.propertyNames();
		while (e.hasMoreElements()) {
			String value = (String) e.nextElement();
			System.out.println("  HDR: '" + value + "' = '" + header.getProperty(value) + "'");
		}
		e = parms.propertyNames();
		while (e.hasMoreElements()) {
			String value = (String) e.nextElement();
			System.out.println("  PRM: '" + value + "' = '" + parms.getProperty(value) + "'");
		}

		return serveFile(uri, header, new File("."), true);
	}

	/**
	 * HTTP response. Return one of these from serve().
	 */
	public class Response {
		/**
		 * Default constructor: response = HTTP_OK, data = mime = 'null'
		 */
		public Response() {
			this.status = HTTP_OK;
		}

		/**
		 * Basic constructor.
		 */
		public Response(String status, String mimeType, InputStream data) {
			this.status = status;
			this.mimeType = mimeType;
			this.data = data;
		}

		/**
		 * Convenience method that makes an InputStream out of given text.
		 */
		public Response(String status, String mimeType, String txt) {
			this.status = status;
			this.mimeType = mimeType;
			this.bytes = txt.getBytes(Charset.forName("UTF-8"));
		}
		
		public Response(String status, String mimeType, byte[] bytes) {
			this.status = status;
			this.mimeType = mimeType;
			this.bytes = bytes;
		}

		/**
		 * Adds given line to the header.
		 */
		public void addHeader(String name, String value) {
			header.put(name, value);
		}

		/**
		 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
		 */
		public String status;

		/**
		 * MIME type of content, e.g. "text/html"
		 */
		public String mimeType;

		/**
		 * Data of the response, may be null.
		 */
		public InputStream data;

		/**
		 * Headers for the HTTP response. Use addHeader() to add lines.
		 */
		public Properties header = new Properties();
		
		public byte[] bytes;
	}
	
	public class WebSocketResponse extends Response {
		public WebSocketResponse(Properties header) {
			super("101 Switching Protocols", null, new byte[]{});
			
			String challenge = header.get("Sec-WebSocket-Key".toLowerCase()).toString();
			challenge += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			
			addHeader("Upgrade", "websocket");
			addHeader("Connection", "Upgrade");
			try {
				addHeader("Sec-WebSocket-Accept", toSHA1(challenge.getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public String toSHA1(byte[] convertme) {
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			byte[] buf = md.digest(convertme);
			return Base64.encodeBytes(buf);
		}
	}

	/**
	 * Some HTTP response status codes
	 */
	public static final String HTTP_OK = "200 OK", HTTP_REDIRECT = "301 Moved Permanently", HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found", HTTP_BADREQUEST = "400 Bad Request", HTTP_INTERNALERROR = "500 Internal Server Error", HTTP_UNAUTHORIZED = "401 Authorization Required", HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT = "text/plain", MIME_HTML = "text/html", MIME_DEFAULT_BINARY = "application/octet-stream", MIME_JSON = "application/json";

	// ==================================================
	// Socket & server code
	// ==================================================

	/**
	 * Starts a HTTP server to given port.
	 * <p>
	 * Throws an IOException if the socket is already in use
	 */
	public NanoHTTPD(int port, boolean ssl, InetAddress bindAddress) throws IOException {
		myTcpPort = port;

		if (ssl) {
			ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
			myServerSocket = ssocketFactory.createServerSocket(port);
		} else {
			if (bindAddress != null) {
				myServerSocket = new ServerSocket(myTcpPort, /* default value */-1, bindAddress);
			} else {
				myServerSocket = new ServerSocket(myTcpPort);
			}
		}
		myThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						Socket s = myServerSocket.accept();
						s.setTcpNoDelay(true);
						
						new HTTPSession(s.getInputStream(), s.getOutputStream(), s.getInetAddress());
					} catch (IOException ioe) {
					}
				}
			}
		});
		myThread.setDaemon(true);
		myThread.start();
	}

	public NanoHTTPD(int port) throws IOException {
		this(port, false, null);
	}

	public NanoHTTPD(int port, InetAddress bindAddress) throws IOException {
		this(port, false, bindAddress);
	}

	/**
	 * Stops the server.
	 */
	public void stop() {
		try {
			myServerSocket.close();
			myThread.interrupt();
			myThread.join();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts as a standalone file server and waits for Enter.
	 * 
	 * public static void main( String[] args ) { System.out.println(
	 * "NanoHTTPD 1.14 (C) 2001,2005-2010 Jarno Elonen\n" +
	 * "(Command line options: [port] [--licence])\n" );
	 * 
	 * // Show licence if requested int lopt = -1; for ( int i=0; i<args.length;
	 * ++i ) if ( args[i].toLowerCase().endsWith( "licence" )) { lopt = i;
	 * System.out.println( LICENCE + "\n" ); }
	 * 
	 * // Change port if requested int port = 80; if ( args.length > 0 && lopt
	 * != 0 ) port = Integer.parseInt( args[0] );
	 * 
	 * if ( args.length > 1 && args[1].toLowerCase().endsWith( "licence" ))
	 * System.out.println( LICENCE + "\n" );
	 * 
	 * NanoHTTPD nh = null; try { nh = new NanoHTTPD( port ); } catch(
	 * IOException ioe ) { System.err.println( "Couldn't start server:\n" + ioe
	 * ); System.exit( -1 ); } nh.myFileDir = new File("");
	 * 
	 * System.out.println( "Now serving files in port " + port + " from \"" +
	 * new File("").getAbsolutePath() + "\"" ); System.out.println(
	 * "Hit Enter to stop.\n" );
	 * 
	 * try { System.in.read(); } catch( Throwable t ) {}; }
	 * 
	 * /** Handles one session, i.e. parses the HTTP request and returns the
	 * response.
	 */
	public class HTTPSession implements Runnable {

		public HTTPSession(InputStream in, OutputStream out, InetAddress addr, boolean blocking) {
			this.in = in;
			this.out = out;
			this.addr = addr;

			if(!blocking) {
				Thread t = new Thread(this);
				t.setDaemon(true);
				t.start();
			}
			else {
				run();
			}
		}
		
		public HTTPSession(InputStream in, OutputStream out, InetAddress addr) {
			this(in, out, addr, false);
		}

		public InputStream in;
		public OutputStream out;
		public InetAddress addr;
		public Lambda<Void, OutputStream> callback = null;
		
		public boolean closeOnCompletion = false;

		public HTTPSession(InputStream in, OutputStream s, InetAddress a, Lambda<Void, OutputStream> callback, boolean blocking) {
			this.in = in;
			this.out = s;
			this.addr = a;
			this.callback = callback;

			if(!blocking) {
				Thread t = new Thread(this);
				t.setDaemon(true);
				t.start();
			}
			else {
				run();
			}
		}
		
		public HTTPSession(InputStream in, OutputStream s, InetAddress a, Lambda<Void, OutputStream> callback) {
			this(in, s, a, callback, false);
		}

		public void run() {
			try {
				final BufferedReader in = new BufferedReader(new InputStreamReader(this.in));

				// Read the request line
				String inLine = in.readLine();
				if (inLine == null)
					return;
				
				if(inLine.substring(0, 2).equalsIgnoreCase("gs")) {
					try {
						String streamLine = inLine;
						do {
							String[] reqLine = streamLine.split(" ", 2);
							
							if(reqLine[0].equalsIgnoreCase("gsa")) {
								continue;
							}
							
							final JSONServer jsonServer = JSONAPI.instance.getJSONServer();
							final String[] split = reqLine[1].split("\\?", 2);
							final OutputStream out = this.out;
							
							(new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										NanoHTTPD.Response r = null;
										if (split.length < 2) {
											r = jsonServer.new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_JSON, jsonServer.returnAPIError("", "Incorrect. Socket requests are in the format PAGE?ARGUMENTS. For example, /api/subscribe?source=....").toJSONString());
										} else {
											Properties header2 = new Properties();
											NanoHTTPD.decodeParms(split[1], header2);
											Properties p = new Properties();
											p.put("X-REMOTE-ADDR", addr.getHostAddress());
											r = jsonServer.serve(split[0], "GET", p, header2);
										}
				
										if (r.data instanceof StreamingResponse) {
											final StreamingResponse s = (StreamingResponse) r.data;
											String line = "";
				
											while ((line = s.nextLine()) != null) {
												try {
													out.write((line + "\r\n").getBytes(Charset.forName("UTF-8")));
												}
												catch (Exception e) {
													StringWriter sw = new StringWriter();
													PrintWriter pw = new PrintWriter(sw);
													e.printStackTrace(pw);
													JSONAPI.dbug("I accidently an exception: " + sw.toString());
													break;
												}
											}
											
											out.close();
											((StreamingResponse)r.data).close();
										} else {
											InputStream res = r.data;
											if(res == null) {
												res = new ByteArrayInputStream(r.bytes);
											}
											
											SlimIOUtils.copy(res, out);
											out.write("\r\n".getBytes(Charset.forName("UTF-8")));
										}
									}
									catch(Exception e) {
										e.printStackTrace();
									}
								}
							})).start();
						} while ((streamLine = in.readLine()) != null);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					return;
				}
				
				StringTokenizer st = new StringTokenizer(inLine);
				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");

				String method = st.nextToken();

				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");

				String uri = st.nextToken();

				// Decode parameters from the URI
				Properties parms = new Properties();
				int qmi = uri.indexOf('?');
				if (qmi >= 0) {
					decodeParms(uri.substring(qmi + 1), parms);
					uri = decodePercent(uri.substring(0, qmi));
				} else
					uri = decodePercent(uri);

				Properties header = new Properties();
				header.put("X-REMOTE-ADDR", addr.getHostAddress());
				
				// If there's another token, it's protocol version,
				// followed by HTTP headers. Ignore version but parse headers.
				// NOTE: this now forces header names uppercase since they are
				// case insensitive and vary by client.
				if (st.hasMoreTokens()) {
					String line = in.readLine();
					while (line != null && line.trim().length() > 0) {
						int p = line.indexOf(':');
						header.put(line.substring(0, p < 0 ? 0 : p).trim().toLowerCase(), line.substring(p + 1).trim());
						line = in.readLine();
					}
				}
				
				// If the method is POST, there may be parameters
				// in data section, too, read it:
				if (method.equalsIgnoreCase("POST")) {
					long size = 0x7FFFFFFFFFFFFFFFl;
					String contentLength = header.getProperty("content-length");
					if (contentLength != null) {
						try {
							size = Integer.parseInt(contentLength);
						} catch (NumberFormatException ex) {
						}
					}
					StringBuffer postLine = new StringBuffer();
					char buf[] = new char[512];
					int read = in.read(buf);

					String contentType = header.getProperty("content-type");
					if (contentType != null && !contentType.equals("x-www-form-urlencoded")) {
						parms = new Properties();

						while (read >= 0 && size > 0) {
							size -= read;
							postLine.append(String.valueOf(buf, 0, read));
							if (size > 0)
								read = in.read(buf);
						}

						parms.put("json", postLine);
					} else {
						while (read >= 0 && size > 0 && !postLine.toString().endsWith("\r\n")) {
							size -= read;
							postLine.append(String.valueOf(buf, 0, read));
							if (size > 0)
								read = in.read(buf);
						}
						decodeParms(postLine.toString().trim(), parms);
					}
				}

				// Ok, now do the serve()
				Response r = serve(uri, method, header, parms);
				if (r == null)
					sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
				else {
					if (r instanceof WebSocketResponse) {
						// write out the upgrade header
						out.write(String.format("HTTP/1.1 %s\r\n", r.status).getBytes(Charset.forName("UTF-8")));
						Enumeration<?> e = r.header.propertyNames();

						while (e.hasMoreElements()) {
							String key = e.nextElement().toString();
							out.write(String.format("%s: %s\r\n", key, r.header.get(key)).getBytes(Charset.forName("UTF-8")));
						}
						out.write("\r\n".getBytes(Charset.forName("UTF-8")));
						out.flush();
						
						// proxy to the websocket server via a websocket client
						// ouch.
						
						JSONTunneledWebSocket ws = new JSONTunneledWebSocket(this.in, out);
						ws.start();
					} else {
						sendResponse(r.status, r.mimeType, r.header, r.data, r.bytes);
					}
				}

				in.close();
				
				if(callback != null) {
					callback.execute(this.out);
				}
			} catch (IOException ioe) {
				try {
					sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				} catch (Throwable t) {
				}
			} catch (InterruptedException ie) {
				
				// Thrown by sendError, ignore and exit the thread.
			}
		}

		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		public String decodePercent(String str) throws InterruptedException {
			try {
				return URLDecoder.decode(str.replace("+", "%2B"), "UTF-8").replace("%2B", "+");
			} catch (Exception e) {
				sendError(HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format ( e.g.
		 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
		 * Properties. NOTE: this doesn't support multiple identical keys due to
		 * the simplicity of Properties -- if you need multiples, you might want
		 * to replace the Properties with a Hastable of Vectors or such.
		 */
		public void decodeParms(String parms, Properties p) throws InterruptedException {
			if (parms == null)
				return;

			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf('=');
				if (sep >= 0)
					p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
			}
		}

		/**
		 * Returns an error message as a HTTP response and throws
		 * InterruptedException to stop furhter request processing.
		 */
		private void sendError(String status, String msg) throws InterruptedException {
			sendResponse(status, MIME_PLAINTEXT, null, null, msg.getBytes());
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse(String status, String mime, Properties header, InputStream data, byte[] bytes) {
			try {
				if (status == null)
					throw new Error("sendResponse(): Status can't be null.");

				PrintWriter pw = new PrintWriter(out);
				pw.print("HTTP/1.1 " + status + " \r\n");

				if (mime != null)
					pw.print("Content-Type: " + mime + "\r\n");

				if (header == null || header.getProperty("Date") == null)
					pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");

				if (header != null) {
					Enumeration<Object> e = header.keys();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = header.getProperty(key);
						pw.print(key + ": " + value + "\r\n");
					}
				}

				pw.print("\r\n");
				pw.flush();

				if (data instanceof StreamingResponse) {
					final StreamingResponse s = (StreamingResponse) data;
					String line = "";

					boolean doContinue = true;
					while (doContinue && (line = s.nextLine()) != null) {
						try {
							out.write((line.trim() + "\r\n").getBytes("UTF-8"));
						} catch (IOException e) {
							doContinue = false;
							out.close();
							if (data != null) {
								data.close();
							}
						}
					}
				} else if (data != null) {
					SlimIOUtils.copy(data, out);
					out.flush();
				} else if (bytes != null) {
					out.write(bytes);
					out.flush();
				}
				// out.flush();
				out.close();
				if (data != null)
					data.close();
			} catch (IOException ioe) {
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
	 * instead of '+'.
	 */
	private String encodeUri(String uri) {
		StringBuffer newUri = new StringBuffer();
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("/"))
				newUri.append("/");
			else if (tok.equals(" "))
				newUri.append("%20");
			else {
				// newUri += URLEncoder.encode( tok );
				// For Java 1.4 you'll want to use this instead:
				try {
					newUri.append(URLEncoder.encode(tok, "UTF-8"));
				} catch (UnsupportedEncodingException uee) {
					uee.printStackTrace();
				}
				;
			}
		}
		return newUri.toString();
	}

	private int myTcpPort;
	private final ServerSocket myServerSocket;
	private Thread myThread;

	File myFileDir;

	// ==================================================
	// File server code
	// ==================================================

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	public Response serveFile(String uri, Properties header, File homeDir, boolean allowDirectoryListing) {
		// Make sure we won't die of an exception later
		if (!homeDir.isDirectory())
			return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");

		// Remove URL arguments
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0)
			uri = uri.substring(0, uri.indexOf('?'));

		// Prohibit getting out of current directory
		if (uri.startsWith("..") || uri.endsWith("..") || uri.indexOf("../") >= 0)
			return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");

		File f = new File(homeDir, uri);
		if (!f.exists())
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Error 404, file not found.");

		// List the directory, if necessary
		if (f.isDirectory()) {
			// Browsers get confused without '/' after the
			// directory, send a redirect.
			if (!uri.endsWith("/")) {
				uri += "/";
				Response r = new Response(HTTP_REDIRECT, MIME_HTML, "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
				r.addHeader("Location", uri);
				return r;
			}

			// First try index.html and index.htm
			if (new File(f, "index.html").exists())
				f = new File(homeDir, uri + "/index.html");
			else if (new File(f, "index.htm").exists())
				f = new File(homeDir, uri + "/index.htm");

			// No index file, list the directory
			else if (allowDirectoryListing) {
				String[] files = f.list();
				String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

				if (uri.length() > 1) {
					String u = uri.substring(0, uri.length() - 1);
					int slash = u.lastIndexOf('/');
					if (slash >= 0 && slash < u.length())
						msg += "<b><a href=\"" + uri.substring(0, slash + 1) + "\">..</a></b><br/>";
				}

				for (int i = 0; i < files.length; ++i) {
					File curFile = new File(f, files[i]);
					boolean dir = curFile.isDirectory();
					if (dir) {
						msg += "<b>";
						files[i] += "/";
					}

					msg += "<a href=\"" + encodeUri(uri + files[i]) + "\">" + files[i] + "</a>";

					// Show file size
					if (curFile.isFile()) {
						long len = curFile.length();
						msg += " &nbsp;<font size=2>(";
						if (len < 1024)
							msg += curFile.length() + " bytes";
						else if (len < 1024 * 1024)
							msg += curFile.length() / 1024 + "." + (curFile.length() % 1024 / 10 % 100) + " KB";
						else
							msg += curFile.length() / (1024 * 1024) + "." + curFile.length() % (1024 * 1024) / 10 % 100 + " MB";

						msg += ")</font>";
					}
					msg += "<br/>";
					if (dir)
						msg += "</b>";
				}
				return new Response(HTTP_OK, MIME_HTML, msg);
			} else {
				return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
			}
		}

		try {
			// Get MIME type from file name extension, if possible
			String mime = null;
			int dot = f.getCanonicalPath().lastIndexOf('.');
			if (dot >= 0)
				mime = (String) theMimeTypes.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
			if (mime == null)
				mime = MIME_DEFAULT_BINARY;

			// Support (simple) skipping:
			long startFrom = 0;
			String range = header.getProperty("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					if (minus > 0)
						range = range.substring(0, minus);
					try {
						startFrom = Long.parseLong(range);
					} catch (NumberFormatException nfe) {
					}
				}
			}

			FileInputStream fis = new FileInputStream(f);
			fis.skip(startFrom);
			Response r = new Response(HTTP_OK, mime, fis);
			r.addHeader("Content-length", "" + (f.length() - startFrom));
			r.addHeader("Content-range", "" + startFrom + "-" + (f.length() - 1) + "/" + f.length());
			return r;
		} catch (IOException ioe) {
			return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}
	}

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static Hashtable<String, String> theMimeTypes = new Hashtable<String, String>();
	static {
		StringTokenizer st = new StringTokenizer("htm		text/html " + "html		text/html " + "txt		text/plain " + "asc		text/plain " + "gif		image/gif " + "jpg		image/jpeg " + "jpeg		image/jpeg " + "png		image/png " + "mp3		audio/mpeg " + "m3u		audio/mpeg-url " + "pdf		application/pdf " + "json		application/json " + "doc		application/msword " + "ogg		application/x-ogg " + "zip		application/octet-stream " + "exe		application/octet-stream " + "class		application/octet-stream ");
		while (st.hasMoreTokens())
			theMimeTypes.put(st.nextToken(), st.nextToken());
	}

	/**
	 * GMT date formatter
	 */
	private static java.text.SimpleDateFormat gmtFrmt;
	static {
		gmtFrmt = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * The distribution licence
	 */
	@SuppressWarnings("unused")
	private static final String LICENCE = "Copyright (C) 2001,2005-2010 by Jarno Elonen <elonen@iki.fi>\n" + "\n" + "Redistribution and use in source and binary forms, with or without\n" + "modification, are permitted provided that the following conditions\n" + "are met:\n" + "\n" + "Redistributions of source code must retain the above copyright notice,\n" + "this list of conditions and the following disclaimer. Redistributions in\n" + "binary form must reproduce the above copyright notice, this list of\n" + "conditions and the following disclaimer in the documentation and/or other\n" + "materials provided with the distribution. The name of the author may not\n" + "be used to endorse or promote products derived from this software without\n" + "specific prior written permission. \n" + " \n" + "THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n" + "IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n" + "OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n" + "IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n" + "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n" + "NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n" + "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n" + "THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" + "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n" + "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
}
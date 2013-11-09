<?php

/**
 * A PHP class for access Minecraft servers that have Bukkit with the {@link http://github.com/alecgorge/JSONAPI JSONAPI} plugin installed.
 * 
 * This class handles everything from key creation to URL creation to actually returning the decoded JSON as an associative array.
 * 
 * @author Alec Gorge <alecgorge@gmail.com>
 * @version Alpha 5
 * @link http://github.com/alecgorge/JSONAPI
 * @package JSONAPI
 * @since Alpha 5
 */
class JSONAPI {
	private $host;
	private $port;
	private $username;
	private $password;
	const URL_FORMAT = 'http://%s:%d/api/2/call?json=%s';
	private $timeout;

	/**
	 * Creates a new JSONAPI instance.
	 */
	public function __construct ($host, $port, $uname, $pword, $salt = '', $timeout = 10) {
		$this->host = $host;
		$this->port = $port;
		$this->username = $uname;
		$this->password = $pword;
		$this->salt = $salt;
		$this->timeout = $timeout;
	}
	
	/**
	 * Generates the proper SHA256 based key from the given method suitable for use as the key GET parameter in a JSONAPI API call.
	 * 
	 * @param string $method The name of the JSONAPI API method to generate the key for.
	 * @return string The SHA256 key suitable for use as the key GET parameter in a JSONAPI API call.
	 */
	public function createKey($method) {
		if(is_array($method)) {
			$method = json_encode($method);
		}
		return hash('sha256', $this->username . $method . $this->password);
	}
	
	/**
	 * Generates the proper URL for a standard API call the given method and arguments.
	 * 
	 * @param string $method The name of the JSONAPI API method to generate the URL for.
	 * @param array $args An array of arguments that are to be passed in the URL.
	 * @return string A proper standard JSONAPI API call URL. Example: "http://localhost:20059/api/call?method=methodName&args=jsonEncodedArgsArray&key=validKey".
	 */
	public function makeURL($method, array $args) {
		return sprintf(self::URL_FORMAT, $this->host, $this->port, rawurlencode(json_encode($this->constructCall($method, $args))));
	}
	
	/**
	 * Generates the proper URL for a multiple API call the given method and arguments.
	 * 
	 * @param array $methods An array of strings, where each string is the name of the JSONAPI API method to generate the URL for.
	 * @param array $args An array of arrays, where each array contains the arguments that are to be passed in the URL.
	 * @return string A proper multiple JSONAPI API call URL. Example: "http://localhost:20059/api/call-multiple?method=[methodName,methodName2]&args=jsonEncodedArrayOfArgsArrays&key=validKey".
	 */
	public function makeURLMultiple(array $methods, array $args) {
		return sprintf(self::URL_FORMAT, $this->host, $this->port, rawurlencode(json_encode($this->constructCalls($methods, $args))));
	}
	
	/**
	 * Calls the single given JSONAPI API method with the given args.
	 * 
	 * @param string $method The name of the JSONAPI API method to call.
	 * @param array $args An array of arguments that are to be passed.
	 * @return array An associative array representing the JSON that was returned.
	 */
	public function call($method, array $args = array()) {
		if(is_array($method)) {
			return $this->callMultiple($method, $args);
		}
		
		$url = $this->makeURL($method, $args);

		return json_decode($this->curl($url), true);
	}
	
	private function curl($url) {
		if(extension_loaded('cURL')) {
			$c = curl_init($url);
			curl_setopt($c, CURLOPT_PORT, $this->port);
			curl_setopt($c, CURLOPT_HEADER, false);
			curl_setopt($c, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($c, CURLOPT_TIMEOUT, $this->timeout);		
			$result = curl_exec($c);
			curl_close($c);
			return $result;
		}else{
			$opts = array('http' =>
				array(
					'timeout' => $this->timeout
					)
				);
			return file_get_contents($url, false, stream_context_create($opts));
		}
	}

	private function constructCall($method, array $args) {
		$json = array();
		$json['name'] = $method;
		$json['arguments'] = $args;
		$json['key'] = $this->createKey($method);
		$json['username'] = $this->username;
		return $json;
	}

	private function constructCalls(array $methods, array $args) {
		$calls = array();
		foreach ($methods as $key => $method) {
			$calls[] = $this->constructCall($method, $args[$key]);
		}
		return $calls;
	}
	
	/**
	 * Calls the given JSONAPI API methods with the given args.
	 * 
	 * @param array $methods An array strings, where each string is the name of a JSONAPI API method to call.
	 * @param array $args An array of arrays of arguments that are to be passed.
	 * @throws Exception When the length of the $methods array and the $args array are different, an exception is thrown.
	 * @return array An array of associative arrays representing the JSON that was returned.
	 */
	public function callMultiple(array $methods, array $args = array()) {
		if(count($methods) !== count($args)) {
			throw new Exception(sprintf('The length of the arrays %s and %s are different! You need an array of arguments for each method!', $methods, $args));
		}
		
		$url = $this->makeURLMultiple($methods, $args);

		return json_decode($this->curl($url), true);
	}
	/**
	 * The default function called if no one matched for JSONAPI.
	 * 
	 * @param string $method The name of the JSONAPI API method to call.
	 * @param array $params An array of arguments that are to be passed.
	 * @return array An associative array representing the JSON that was returned.
	 */
	function __call($method, $params) {
        if(is_array($params)) {
            return $this->call($method, $params);
        } else {
            return $this->call($method, array($params));	
        }
	}

	/**
	 * @return string
	 */
	public function getHost() {
	    return $this->host;
	}
	
	/**
	 * @param string $newhost
	 * @return JSONAPI This object, for method chaining
	 */
	public function setHost($host) {
	    $this->host = $host;
	
	    return $this;
	}

	/**
	 * @return integer
	 */
	public function getPort() {
	    return $this->port;
	}
	
	/**
	 * @param integer $newport
	 * @return JSONAPI This object, for method chaining
	 */
	public function setPort($port) {
		$port = (int) $port;
		if ($port < 1 || $port > 65535) {
			throw new Exception('The port must be between 1 and 65535, you supplied ' . $port);
		}
	    $this->port = $port;
	
	    return $this;
	}

	/**
	 * @return string
	 */
	public function getUsername() {
	    return $this->username;
	}
	
	/**
	 * @param string $newusername
	 * @return JSONAPI This object, for method chaining
	 */
	public function setUsername($username) {
	    $this->username = $username;
	
	    return $this;
	}

	/**
	 * @return string
	 */
	public function getPassword() {
	    return $this->password;
	}
	
	/**
	 * @param string $newpassword
	 * @return JSONAPI This object, for method chaining
	 */
	public function setPassword($username) {
	    $this->password = $password;
	
	    return $this;
	}

	/**
	 * @return integer
	 */
	public function getTimeout() {
	    return $this->timeout;
	}
	
	/**
	 * @param integer $newtimeout
	 * @return JSONAPI This object, for method chaining
	 */
	public function setTimeout($timeout) {
	    $this->timeout = (int) $timeout;
	
	    return $this;
	}
}

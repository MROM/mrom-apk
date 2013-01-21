<?php
	$ip = $_GET['ip'];
	$build = $_GET['build'];
	$device_id = $_GET['device_id'];
	$con = mysql_connect('localhost', '', '');
	if (!$con) {
		die('Could not connect: ' . mysql_error());
	}
	if ($ip == null) {
		die ("Error IP variable cannot be null");
	}	
	if ($build == null) {
		die ("Error BUILD variable cannot be null");
	}	
	mysql_select_db('montuorinet', $con);
	$sql = "INSERT INTO mrom_checkin (ip, build, device_id) VALUES ('$ip','$build','$device_id')";
	if (!mysql_query($sql,$con)) {
  		die('Error: ' . mysql_error());
  	}
	list($a1, $a2, $a3, $a4, $a5, $a6, $a7) = explode(':', $build, 7);
	//print "build: $build<br>";
	//print "version: $a6<br>";
	function escape($str) {
		$str = str_replace("\\", "\\\\", $str);
		$str = str_replace("'", "''", $str);
		return $str;
	}
        $sth = mysql_query("SELECT * FROM mrom_conf where android_version='".escape($a6)."'", $con) or die('Error: ' . mysql_error());
        while ($r = mysql_fetch_object($sth)) {
		$rows[] = $r;
	}
        mysql_close($con);
        print json_encode($rows);
?>

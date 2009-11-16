<?php
$filename = "dksRefs";
$parameters = "parameters";

if(isset($_GET["reset"]) && $_GET["reset"] != null) {
	if (is_writable($filename)) {

	   // In our example we're opening $filename in append mode.
	   // The file pointer is at the bottom of the file hence
	   // that's where $somecontent will go when we fwrite() it.
	   if ((!$handle = fopen($filename, 'w')) || (!$handle1 = fopen($parameters, 'w'))) {
		 echo "Cannot open file ($filename) and ($parameters)";
		 exit;
	   }else
		echo "UPDATE-OK";
	   fclose($handle);
	   fclose($handle1);
          
	} else {
	   echo "The file $filename is not writable";
	   exit;
	}
	exit;
}

if(isset($_GET["addDKSParameters"]) && $_GET["addDKSParameters"] != null && $_GET['addDKSParameters'] != '') {
	// Let's make sure the file exists and is writable first.
	if (is_writable($parameters)) {

	   // In our example we're opening $filename in append mode.
	   // The file pointer is at the bottom of the file hence
	   // that's where $somecontent will go when we fwrite() it.
	   if (!$handle1 = fopen($parameters, 'a')) {
		 echo "Cannot open file ($parameters)";
		 exit;
	   }

	   // Write $somecontent to our opened file.
	   if (fwrite($handle1, $_GET['addDKSParameters']."\n") === FALSE) {
	       echo "Cannot write to file ($parameters)";
	       exit;
	   }else
		echo "UPDATE-OK";
	   fclose($handle1);
	} else {
	   echo "The file $filename is not writable";
	   exit;
	}
	exit;
}
if(isset($_GET["addDKSRef"]) && $_GET["addDKSRef"] != null && $_GET['addDKSRef'] != '') {
	// Let's make sure the file exists and is writable first.
	if (is_writable($filename)) {

	   // In our example we're opening $filename in append mode.
	   // The file pointer is at the bottom of the file hence
	   // that's where $somecontent will go when we fwrite() it.
	   if (!$handle = fopen($filename, 'a')) {
		 echo "Cannot open file ($filename)";
		 exit;
	   }

	   // Write $somecontent to our opened file.
	   if (fwrite($handle, $_GET['addDKSRef']."\n") === FALSE) {
	       echo "Cannot write to file ($filename)";
	       exit;
	   }else
		echo "UPDATE-OK";
	   fclose($handle);
	} else {
	   echo "The file $filename is not writable";
	   exit;
	}
	exit;
}

$hosts = @file($filename);
$rand = rand(0, sizeof($hosts)-1);
for($i = $rand; $i < sizeof($hosts); $i++) {
	
	//if(ereg("^dksref\:\/\/193\.10\.64\.", $hosts[$i]) || ereg("^dksref\:\/\/130\.104\.72\.", $hosts[$i]))
	//Controlling that is a valid DKS REF address
	if(ereg("^dks\:\/\/[0-9\.]+", $hosts[$i]) || ereg("^dks\:\/\/[0-9\.]+", $hosts[$i]))
		echo $hosts[$i];
}
$hosts = @file($filename);
$rand = rand(0, sizeof($hosts)-1);
for($i = $rand; $i < sizeof($hosts); $i++) {
        if(ereg("^dksref\:\/\/193\.10\.64\.", $hosts[$i]) || ereg("^dksref\:\/\/130\.104\.72\.", $hosts[$i]))
                echo $hosts[$i];
}
for($i = 0; $i < $rand; $i++) {
        if(ereg("^dksref\:\/\/193\.10\.64\.", $hosts[$i]) || ereg("^dksref\:\/\/130\.104\.72\.", $hosts[$i]))
                echo $hosts[$i];
}


?>

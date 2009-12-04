<%-- 
    Document   : convert
    Created on : Dec 4, 2009, 3:47:21 PM
    Author     : saibbot
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Currency Coverter v0.0</h1>
        <form name="inputData" action="/FrontController">
            <h2>From currency:</h2>
            <select name="fromcurrency">
                <option value="EUR">EUR</option>
                <option value="USD">USD</option>
                <option value="GBP">GBP</option>
                <option value="LVL">LVL</option>
                <option value="SEK">SEK</option>
            </select>
            &nbsp; Amount: <input type="text" name="fromamount" value="1" size="5" />
        </form>
    </body>
</html>

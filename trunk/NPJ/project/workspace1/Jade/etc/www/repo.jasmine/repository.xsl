<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">

		<html>
			<body>
				<font face="sans-serif">
					<div style="text-align: center;">						
						<font size="+3">JADE Bundle Repository</font>
						<hr/>
						<a href="#jbr">What is JBR?</a>
						|
						<a href="#bundles">Bundles</a>
						|
						<a href="#contact">Contact</a>
						<hr/>
					</div>
					<i>
						<h1>News</h1>
						<ul>
							<li>
								Added the Joram Server, PostgresqlG,
								Derby, DerbyG, ClusterDBInterfaces,
								Sequoia, Escada,
								and IDGenerator Bundles to the repository.
								(May 25th, 2007)
							</li>
							<li>
								Added the Apps Bundle, Catalina Bundle,
								Commons Bundle, Jonas Interfaces Bundle,
								and Tools Bundle to the repository.
								(February 6th, 2006)
							</li>
							<li>
								Added the Property Commands bundle 
								to the repository. (November 15th, 2005)
							</li>
							<li>
								Added the MySQL Sci bundle to the
								repository. (October 3rd, 2005)
							</li>
							<li>
								Added the Apache Sci bundle to the
								repository. (October 3rd, 2005)
							</li>
							<li>
								Added the Rubis MySQL bundle to the
								repository. (September 29th, 2005)
							</li>
							<li>
								Added the MySQL Linux bundle to the
								repository. (September 29th, 2005)
							</li>
							<li>
								Added the Rubis Tomcat bundle to the
								repository. (September 28th, 2005)
							</li>
							<li>
								Added the Tomcat Linux bundle to the
								repository. (September 28th, 2005)
							</li>
							<li>
								Added the Rubis Apache bundle to the
								repository. (September 22nd, 2005)
							</li>
							<li>
								Added the Apache Linux bundle to the
								repository. (September 20th, 2005)
							</li>
							<li>
								Added the Fractal Factory bundle to the
								repository. (September 20th, 2005)
							</li>
						</ul>
						<small>
							(
							<a href="../news.html">News archive</a>
							)
						</small>
					</i>
					<hr/>
					<a name="jbr"/>
					<h2>What is JBR?</h2>
					<p>
						JBR, a clone of
						<a href="http://oscar-osgi.sourceforge.net/">
							OBR
						</a>
						, is a repository of OSGi bundles that wrap
						<a href="http://fractal.objectweb.org">
							Fractal
						</a>
						components that can be deployed in the
						<a href="http://sardes.inrialpes.fr/research/jade/">
							JADE
						</a>
						autonomic system and other bundles related to
						deployment in JADE.
					</p>
					<p>
						The goal of JBR is to provide the administrators
						of JADE-managed systems a set of packages
						(bundles) that they can deploy in these systems.
					</p>
					<a name="bundles"/>
					<h2>Bundles</h2>
					<p>
						The following is a complete list of all bundles
						in the JBR repository. Not all bundles are
						hosted on this site and individual bundles have
						varying licenses; please consult the individual
						bundle documentation for details. The source for
						these bundles is an excellent way to learn how
						to implement bundles, but you may also want to
						review the
						<a href="http://oscar-osgi.sf.net/tutorial/">
							bundle tutorial
						</a>
						.
					</p>
					<ul>
						<xsl:for-each select="bundles/bundle">
							<xsl:sort select="bundle-name"/>
							<li>
								<b>
									<xsl:value-of select="bundle-name"/>
								</b>
								(
								<a href="{bundle-docurl}">docs</a>
								|
								<a href="{bundle-updatelocation}">
									bundle
								</a>
								|
								<a href="{bundle-sourceurl}">source</a>
								) -
								<xsl:value-of select="bundle-description"/>
							</li>
						</xsl:for-each>
					</ul>
					<a name="contact"/>
					<h2>Contact</h2>
					<p>
						Any questions? Want to include your bundle? Want
						to contribute? Contact
						<a href="mailto:jakub.kornas@inrialpes.fr">
							Jakub Kornas
						</a>
						.
					</p>
				</font>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
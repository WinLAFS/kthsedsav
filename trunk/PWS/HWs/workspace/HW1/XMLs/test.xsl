<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"/>
	<xsl:template match="/files">
		<profile xmlns="http://www.kth.se/profile" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.kth.se/profile profile.xsd">
			<name><xsl:value-of select="/files/CV/name"></xsl:value-of></name>
			<surname><xsl:value-of select="/files/CV/surname"></xsl:value-of></surname>
			<birthDate>
				<xsl:value-of select="/files/CV/birthDate"></xsl:value-of>
			</birthDate>

			<xsl:copy-of select="/files/CV/name"></xsl:copy-of>
		</profile>
	</xsl:template>
</xsl:stylesheet>
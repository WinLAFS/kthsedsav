<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="xml" />
	<xsl:template match="/files">

		<profile xmlns="http://www.kth.se/profile" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.kth.se/profile profile.xsd">
			<xsl:element name="name">
				<xsl:value-of select="/files/CV/name"></xsl:value-of>
			</xsl:element>
			<xsl:element name="surname">
				<xsl:value-of select="/files/CV/surname"></xsl:value-of>
			</xsl:element>
			<xsl:element name="birthDate">
				<xsl:value-of select="/files/CV/birthDate"></xsl:value-of>
			</xsl:element>

			<xsl:element name="degrees">
				<xsl:for-each select="/files/degrees/degree">

					<xsl:element name="degree">
						<xsl:attribute name="title">
						<xsl:value-of select="./@title"></xsl:value-of>
					</xsl:attribute>
						<xsl:element name="university">
							<xsl:value-of select="./university"></xsl:value-of>
						</xsl:element>
						<xsl:element name="startYear">
							<xsl:value-of select="./startYear"></xsl:value-of>
						</xsl:element>
						<xsl:element name="endYear">
							<xsl:value-of select="./endYear"></xsl:value-of>
						</xsl:element>
						<xsl:element name="subject">
							<xsl:value-of select="./subject"></xsl:value-of>
						</xsl:element>
						<xsl:element name="grades">
							<xsl:for-each select="./grades/grade">
								<xsl:element name="grade">
									<xsl:attribute name="courseID">
									<xsl:value-of select="./@courseID"></xsl:value-of>
									</xsl:attribute>
									
									<xsl:element name="gradeVal">
										<xsl:value-of select="./gradeVal"></xsl:value-of>
									</xsl:element>
								</xsl:element>
							</xsl:for-each>
						</xsl:element>
						<xsl:variable name="avga"
							select="sum(./grades/grade/gradeVal) div count(./grades/grade/gradeVal)"></xsl:variable>
						<xsl:element name="gpa">
							<xsl:value-of select="$avga"></xsl:value-of>
						</xsl:element>


					</xsl:element>
				</xsl:for-each>
			</xsl:element>

			<records>
				<xsl:for-each select="/files/records/record">
					<record>
						<fromDate>
							<xsl:value-of select="fromDate"></xsl:value-of>
						</fromDate>
						<toDate>
							<xsl:value-of select="toDate"></xsl:value-of>
						</toDate>
						<position>
							<xsl:value-of select="position"></xsl:value-of>
						</position>
						<xsl:variable name="cur_company" select="@companyName"></xsl:variable>
						<site>
							<xsl:value-of
								select="/files/companies/company[@companyName=$cur_company]/site"></xsl:value-of>
						</site>
					</record>
				</xsl:for-each>
			</records>


		</profile>
	</xsl:template>
</xsl:stylesheet>
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:extract="http://geo.so.ch/schema/AGI/Cadastre/0.9/Extract" exclude-result-prefixes="extract" version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:decimal-format name="swiss" decimal-separator="." grouping-separator="'"/>
  <xsl:template match="extract:GetExtractByIdResponse">
    <fo:root font-family="Cadastra" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsd="https://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="firstPage" page-height="297mm" page-width="210mm" margin-top="10mm" margin-bottom="10mm" margin-left="12mm" margin-right="10mm">
          <fo:region-body margin-top="45mm" background-color="#FFFFFF"/>
          <fo:region-before extent="40mm" background-color="#FFFFFF"/>
          <fo:region-after extent="10mm" background-color="#FFFFFF" display-align="after"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="middlePage" page-height="297mm" page-width="210mm" margin-top="10mm" margin-bottom="10mm" margin-left="12mm" margin-right="10mm">
          <fo:region-body margin-top="15mm" background-color="#FFFFFF"/>
          <fo:region-before extent="10mm" background-color="#FFFFFF"/>
          <fo:region-after extent="10mm" background-color="#FFFFFF" display-align="after"/>
        </fo:simple-page-master>
        <fo:page-sequence-master master-name="allPages">
          <fo:repeatable-page-master-alternatives>
            <fo:conditional-page-master-reference page-position="first" master-reference="firstPage"/>
            <fo:conditional-page-master-reference page-position="rest" master-reference="middlePage"/>
          </fo:repeatable-page-master-alternatives>
        </fo:page-sequence-master>
      </fo:layout-master-set>
      <xsl:apply-templates/>
    </fo:root>
  </xsl:template>
  <xsl:template match="extract:Extract">
    <fo:page-sequence master-reference="allPages" id="my-sequence-id">
      <fo:static-content flow-name="xsl-region-before">
        <fo:block>
          <fo:block-container absolute-position="absolute" top="0mm" left="123mm" background-color="#FFFFFF">
            <fo:block>
              <fo:external-graphic height="6.7mm" width="60mm" content-width="scale-to-fit" content-height="scale-to-fit">
                <xsl:attribute name="src">
                  <xsl:text>url('data:</xsl:text>
                  <xsl:text>image/png;base64,</xsl:text>
                  <xsl:value-of select="extract:CantonalLogo"/>
                  <xsl:text>')</xsl:text>
                </xsl:attribute>
              </fo:external-graphic>
            </fo:block>
          </fo:block-container>
        </fo:block>
        <fo:retrieve-marker retrieve-class-name="subHeader" retrieve-position="first-starting-within-page"/>
      </fo:static-content>
      <fo:static-content flow-name="xsl-region-after">
        <fo:table table-layout="fixed" width="100%" margin-top="4mm" font-size="6.5pt" font-style="normal" font-weight="400" font-family="Cadastra">
          <fo:table-column column-width="50%"/>
          <fo:table-column column-width="50%"/>
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell>
                <fo:block>
                  <xsl:value-of select="format-dateTime(extract:CreationDate,'[Y0001]-[M01]-[D01] [H01]:[m01]:[s01]')"/>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell text-align="right">
                <fo:block>Seite <fo:page-number/>/<fo:page-number-citation-last ref-id="my-sequence-id"/></fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:static-content>
      <xsl:apply-templates select="extract:RealEstate"/>
    </fo:page-sequence>
  </xsl:template>
  <xsl:template match="extract:RealEstate">
    <fo:flow flow-name="xsl-region-body">
        <fo:block-container font-style="italic" font-weight="400" font-family="Cadastra">
      <fo:block>

          <xsl:text>fubar</xsl:text>
      </fo:block>

        </fo:block-container>
    </fo:flow>
  </xsl:template>

</xsl:stylesheet>

package net.teknoraver.ryanair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

class FlightRequest {
	String from, to, date;
	private int hc;

	FlightRequest(String f, String t, String d) {
		if(f == null || t == null || d == null)
			throw new IllegalArgumentException("Bad arguments");
		from = f;
		to = t;
		date = d;
	}

	@Override
	public boolean equals(Object o) {
		if(o == this)
			return true;

		if (!(o instanceof FlightRequest))
			return false;

		FlightRequest f = (FlightRequest)o;

		return	from.equals(f.from) &&
			to.equals(f.to) &&
			date.equals(f.date);
	}

	@Override
	public int hashCode() {
		if(hc == 0)
			hc = (from + to + date).hashCode();
		return hc;
	}
}

class FlightResult extends DefaultHandler {
	private final static Pattern timepatt = Pattern.compile("(\\d{2}:\\d{2})$");
	private final static Pattern pricepatt = Pattern.compile("(\\d{1,3}\\.\\d{2})\\W+(\\w+)");
	private boolean departb, arriveb, priceb;
	String depart, arrive, price;
	Date timestamp = new Date();


	FlightResult(InputStream in) throws Exception {		
		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(this);
		xr.setErrorHandler(this);

		Vector<InputStream> pipe = new Vector<InputStream>();

		pipe.add(new ByteArrayInputStream("<flight>".getBytes()));
		pipe.add(in);
		pipe.add(new ByteArrayInputStream("</flight>".getBytes()));

		xr.parse(new InputSource(new SequenceInputStream(pipe.elements())));
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		if(qName.equals("div"))
			for(int i = 0; i < atts.getLength(); i++)
				if(atts.getQName(i).equals("id") && atts.getValue(i).equals("grandtotal"))
					priceb = true;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String data = new String(ch, start, length);
		if(departb) {
			departb = false;

			Matcher m = timepatt.matcher(data);
			if(m.find())
				depart = m.group(1);
		} else if(arriveb) {
			arriveb = false;

			Matcher m = timepatt.matcher(data);
			if(m.find())
				arrive = m.group(1);
		} else if(priceb) {
			priceb = false;

			Matcher m = pricepatt.matcher(data);
			if(m.find())
				price = m.group(1) + ' ' + m.group(2);
		} else {
			if(data.contains("Depart:"))
				departb = true;
			else if(data.contains("Arrive:"))
				arriveb = true;
		}
	}

	@Override
	public String toString() {
		return depart + "|" + arrive + "|" + price;
	}

	@Override public void startDocument() { }
	@Override public void endElement(String arg0, String arg1, String arg2) { }
	@Override public void endDocument() { }
	@Override public void endPrefixMapping(String arg0) { }
	@Override public void ignorableWhitespace(char[] arg0, int arg1, int arg2) { }
	@Override public void processingInstruction(String arg0, String arg1) { }
	@Override public void setDocumentLocator(Locator arg0) { }
	@Override public void skippedEntity(String arg0) throws SAXException { }
	@Override public void startPrefixMapping(String arg0, String arg1) { }
}

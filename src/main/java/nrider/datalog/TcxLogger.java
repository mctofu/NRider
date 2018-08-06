package nrider.datalog;

import nrider.io.PerformanceData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class TcxLogger extends BaseLogger {
    private String _id;

    public TcxLogger(String id) {
        _id = id;
    }

    public void close() {
        Log log = computeLog();

        if (log.isEmpty()) {
            return;
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element tcd = doc.createElement("TrainingCenterDatabase");
            tcd.setAttribute("xmlns", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2");
            tcd.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            tcd.setAttribute("xsi:schemaLocation", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd");
            doc.appendChild(tcd);

            Element activities = doc.createElement("Activities");
            tcd.appendChild(activities);

            Element activity = doc.createElement("Activity");
            activity.setAttribute("Sport", "Biking");
            activities.appendChild(activity);

            activity.appendChild(createSimpleNode(doc, "Id", getDate(System.currentTimeMillis())));

            Element lap = doc.createElement("Lap");

            lap.setAttribute("StartTime", getDate(log.getStartTime()));
            activity.appendChild(lap);

            lap.appendChild(createSimpleNode(doc, "TotalTimeSeconds", "" + log.getDuration() / 1000));
            lap.appendChild(createSimpleNode(doc, "DistanceMeters", "0"));
            lap.appendChild(createSimpleNode(doc, "MaximumSpeed", "0"));
            lap.appendChild(createSimpleNode(doc, "Calories", "0"));
            lap.appendChild(createSimpleNode(doc, "Intensity", "Active"));
            lap.appendChild(createSimpleNode(doc, "TriggerMethod", "Manual"));

            Element track = doc.createElement("Track");
            lap.appendChild(track);

            NumberFormat intFormat = NumberFormat.getIntegerInstance();
            NumberFormat decFormat = new DecimalFormat("0.0");

            for (LogEntry entry : log.getEntries()) {
                Element trackPoint = doc.createElement("Trackpoint");
                trackPoint.appendChild(createSimpleNode(doc, "Time", getDate(entry.getTimeStamp())));
                track.appendChild(trackPoint);

                HashMap<PerformanceData.Type, Float> values = entry.getValues();

                if (values.containsKey(PerformanceData.Type.EXT_HEART_RATE)) {
                    trackPoint.appendChild(createValueNode(doc, "HeartRateBpm", intFormat.format(values.get(PerformanceData.Type.EXT_HEART_RATE))));
                }
                if (values.containsKey(PerformanceData.Type.EXT_CADENCE)) {
                    trackPoint.appendChild(createSimpleNode(doc, "Cadence", intFormat.format(values.get(PerformanceData.Type.EXT_CADENCE))));
                }
                if (values.containsKey(PerformanceData.Type.POWER) || values.containsKey(PerformanceData.Type.SPEED)) {
                    Element extensions = doc.createElement("Extensions");
                    Element tpx = doc.createElement("TPX");
                    tpx.setAttribute("xmlns", "http://www.garmin.com/xmlschemas/ActivityExtension/v2");
                    extensions.appendChild(tpx);
                    if (values.containsKey(PerformanceData.Type.SPEED)) {
                        tpx.appendChild(createSimpleNode(doc, "Speed", decFormat.format(values.get(PerformanceData.Type.SPEED))));
                    }
                    if (values.containsKey(PerformanceData.Type.POWER)) {
                        tpx.appendChild(createSimpleNode(doc, "Watts", intFormat.format(values.get(PerformanceData.Type.POWER))));
                    }

                    trackPoint.appendChild(extensions);
                }
            }

            Element creator = doc.createElement("Creator");
            activity.appendChild(creator);
            creator.setAttribute("xsi:type", "Device_t");
            creator.appendChild(createSimpleNode(doc, "Name", "EDGE705"));
            creator.appendChild(createSimpleNode(doc, "UnitId", "3489680639"));
            creator.appendChild(createSimpleNode(doc, "ProductID", "625"));
            Element version = doc.createElement("Version");
            creator.appendChild(version);
            version.appendChild(createSimpleNode(doc, "VersionMajor", "2"));
            version.appendChild(createSimpleNode(doc, "VersionMinor", "70"));
            version.appendChild(createSimpleNode(doc, "BuildMajor", "0"));
            version.appendChild(createSimpleNode(doc, "BuildMinor", "0"));

            Element author = doc.createElement("Author");
            author.setAttribute("xsi:type", "Application_t");
            tcd.appendChild(author);
            author.appendChild(createSimpleNode(doc, "Name", "EDGE705"));
            Element build = doc.createElement("Build");
            author.appendChild(build);
            Element buildVersion = doc.createElement("Version");
            build.appendChild(buildVersion);
            buildVersion.appendChild(createSimpleNode(doc, "VersionMajor", "2"));
            buildVersion.appendChild(createSimpleNode(doc, "VersionMinor", "70"));
            buildVersion.appendChild(createSimpleNode(doc, "BuildMajor", "0"));
            buildVersion.appendChild(createSimpleNode(doc, "BuildMinor", "0"));
            build.appendChild(createSimpleNode(doc, "Type", "Release"));
            author.appendChild(createSimpleNode(doc, "LangID", "EN"));
            author.appendChild(createSimpleNode(doc, "PartNumber", "006-B0625-00"));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new FileWriter(_id + ".tcx"));
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new Error("Some sort of parser error", e);
        }
    }

    private String getDate(long timeStamp) {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal).toXMLFormat();
        } catch (DatatypeConfigurationException dce) {
            throw new Error("Can't handle", dce);
        }
    }

    private Node createSimpleNode(Document doc, String name, String value) {
        Element el = doc.createElement(name);
        el.setTextContent(value);
        return el;
    }

    private Node createValueNode(Document doc, String name, String value) {
        Element el = doc.createElement(name);
        Element valueEl = doc.createElement("Value");
        valueEl.setTextContent(value);
        el.appendChild(valueEl);
        return el;
    }
}


package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import javax.swing.JSplitPane;
import java.io.PrintWriter;

public class BurpExtender implements IBurpExtender, IHttpListener, IMessageEditorController {
	
	private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private IMessageEditor requestViewer;
    private IHttpRequestResponse currentlyDisplayedItem;
    private PrintWriter stdout;
	
	public void registerExtenderCallbacks (IBurpExtenderCallbacks callbacks) {
		
		this.callbacks = callbacks;
        // obtain our output stream
        stdout = new PrintWriter(callbacks.getStdout(), true);
		
		callbacks.setExtensionName("test burp");
		
		helpers = callbacks.getHelpers();
		
		callbacks.registerHttpListener(this);
		
		// requestViewer = callbacks.createMessageEditor(BurpExtender.this, false);		
		
	}
	
	@Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
		callbacks.createMessageEditor(BurpExtender.this, false);
		String aa = helpers.bytesToString(messageInfo.getRequest());
		Pattern p = Pattern.compile("(GET .*?HTTP)");
		Matcher m = p.matcher(aa);
		String aap = "";
		if (m.find()) {
			aap = m.group();			
		}
		
		LogEntry log = new LogEntry(toolFlag, callbacks.saveBuffersToTempFiles(messageInfo), 
                helpers.analyzeRequest(messageInfo).getUrl());
		
		requestViewer = callbacks.createMessageEditor(BurpExtender.this, false);
		
        stdout.println(
        		/*
                (messageIsRequest ? "HTTP request to " : "HTTP response from ") +
                messageInfo.getHttpService() +
                " [" + callbacks.getToolName(toolFlag) + "]");
                */
        		(messageIsRequest ? "HTTP request to " : "HTTP response from ") +
                helpers.bytesToString(messageInfo.getRequest()) + ",," + messageInfo.getHttpService().getPort() +
                messageInfo.getHttpService().getProtocol() + ",," + aap + ",,," + helpers.bytesToString(log.requestResponse.getResponse()) +
                " [" + callbacks.getToolName(toolFlag) + "]");
    }


	@Override
    public byte[] getRequest()
    {
        return currentlyDisplayedItem.getRequest();
    }

    @Override
    public byte[] getResponse()
    {
        return currentlyDisplayedItem.getResponse();
    }
    
    @Override
    public IHttpService getHttpService()
    {
        return currentlyDisplayedItem.getHttpService();
    }
    
    private static class LogEntry
    {
        final int tool;
        final IHttpRequestResponsePersisted requestResponse;
        final URL url;

        LogEntry(int tool, IHttpRequestResponsePersisted requestResponse, URL url)
        {
            this.tool = tool;
            this.requestResponse = requestResponse;
            this.url = url;
        }
    }
}
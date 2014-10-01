import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

/**
 * Wrapper class around Wiki pages to facilitate operating on their controls.
 * @author smootp
 */
public class WikiPage {
	private HtmlPage page;
	private HtmlForm form;
	
	public WikiPage(Page page) {
		this.page = (HtmlPage) page;
	}
	
	public HtmlPage getPage() {
		return page;
	}
	
	public WikiPage withFormName(String formName)  {
		form = page.getFormByName(formName);
		return this;
	}
	
	public WikiPage withFormId(String formId) {
		form = (HtmlForm) page.getElementById(formId);
		return this;
	}
	
	public void setCheckboxInputValue(String inputControlName, boolean newValue) throws IOException  {
		final HtmlCheckBoxInput control = form.getInputByName(inputControlName);
		
		control.click();
	}

	public void setTextAreaValue(String textAreaName, String text)  {
		HtmlTextArea textArea = form.getTextAreaByName(textAreaName);
		textArea.setText(text);
	}

	public void setFileInputValue(String fileInputName, File inputFile) throws MalformedURLException, IOException  {
		HtmlFileInput fileInputElement = form.getInputByName(fileInputName);
		fileInputElement.setValueAttribute(inputFile.getAbsolutePath());
		fileInputElement.setContentType(inputFile.toURI().toURL().openConnection().getContentType());
	}

	public void setTextInputValue(String inputControlName, String newValue) throws IOException  {
		final HtmlInput control = form.getInputByName(inputControlName);
		control.type(newValue);
	}

	public HtmlPage clickSubmit(String submitControlName) throws IOException  {
		final HtmlSubmitInput submitControl = form.getInputByName(submitControlName);
		return submitControl.click();
	}
}
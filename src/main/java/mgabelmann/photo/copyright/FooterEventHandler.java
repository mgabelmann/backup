package mgabelmann.photo.copyright;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles events related to the footer of a page.
 * The total number of pages is not included until the document is completed.
 *
 */
public class FooterEventHandler implements IEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FooterEventHandler.class);

    private PdfFormXObject placeholder;

    /**
     * Constructor.
     */
    public FooterEventHandler() {
        this.placeholder = new PdfFormXObject(new Rectangle(0,0, 20, 20));
    }

    @Override
    public void handleEvent(Event e) {
        LOGGER.debug("handleEvent {}", e.getType());

        PdfDocumentEvent event = (PdfDocumentEvent) e;
        PdfDocument pdf = event.getDocument();
        PdfPage page = event.getPage();

        int pageNumber = pdf.getPageNumber(page);
        Rectangle pageSize = page.getPageSize();

        PdfCanvas pdfCanvas = new PdfCanvas(page);
        Canvas canvas = new Canvas(pdfCanvas, pageSize);

        Paragraph p1 = new Paragraph().add("" + pageNumber).add(" of ");
        p1.setFontSize(10);

        canvas.showTextAligned(p1, 300f, 25f, TextAlignment.RIGHT);
        canvas.close();

        pdfCanvas.addXObjectAt(placeholder, 300 + 4.5f, 25 - 3f);
        pdfCanvas.release();
    }

    public void writeTotal(PdfDocument pdf) {
        Canvas canvas = new Canvas(placeholder, pdf);
        canvas.setFontSize(10);
        canvas.showTextAligned("" + pdf.getNumberOfPages(), 0f, 3f, TextAlignment.LEFT);
        canvas.close();
    }

}

package mgabelmann.photo.copyright;

/**
 *
 */
public class FooterPdfPageEventHelper {

}

/*
public class FooterPdfPageEventHelper extends PdfPageEventHelper {
    private final Font f = new Font(Font.FontFamily.COURIER, 8, Font.NORMAL);

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer(writer.getPageNumber()),
                (document.right() - document.left()) / 2 + document.leftMargin(),
                document.top() + 10, 0);
    }

    private Phrase footer(final int page) {
        return new Phrase("Page " + page, f);
    }
}
*/

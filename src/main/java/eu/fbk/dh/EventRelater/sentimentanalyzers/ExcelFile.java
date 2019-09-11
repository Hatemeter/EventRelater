package eu.fbk.dh.EventRelater.sentimentanalyzers;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;

public class ExcelFile {
    private OPCPackage pkg;
    private XSSFWorkbook wb;
    private XSSFSheet sheet;
    private HSSFRow row;
    private HSSFCell cell;
    private int rows;

    public ExcelFile(){

    }

    public ExcelFile(File file) throws IOException, InvalidFormatException {
        pkg=OPCPackage.open(file);
        wb=new XSSFWorkbook(pkg);
        sheet=wb.getSheetAt(0);
        rows=sheet.getPhysicalNumberOfRows();
    }

    public OPCPackage getPkg() {
        return pkg;
    }

    public void setPkg(OPCPackage pkg) {
        this.pkg = pkg;
    }

    public XSSFWorkbook getWb() {
        return wb;
    }

    public void setWb(XSSFWorkbook wb) {
        this.wb = wb;
    }

    public XSSFSheet getSheet() {
        return sheet;
    }

    public void setSheet(XSSFSheet sheet) {
        this.sheet = sheet;
    }

    public HSSFRow getRow() {
        return row;
    }

    public void setRow(HSSFRow row) {
        this.row = row;
    }

    public HSSFCell getCell() {
        return cell;
    }

    public void setCell(HSSFCell cell) {
        this.cell = cell;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}

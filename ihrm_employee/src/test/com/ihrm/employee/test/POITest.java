package com.ihrm.employee.test;

import com.ihrm.common.handler.SheetHandler;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;

/**
 * 使用事件模型解析百万数据excel报表
 */
public class POITest {
    public static void main(String[] args) throws Exception {

        //demo.xlsx 在 resources/excel-template下
        String path = "D:\\BaiduNetdiskDownload\\POI高级\\资源\\百万数据报表\\demo.xlsx";
        //1.根据excel报表获取OPCPackage
        OPCPackage open = OPCPackage.open(path, PackageAccess.READ);
        //2.创建XSSFReader
        XSSFReader reader = new XSSFReader(open);
        //3.获取SharedStringTable对象
        SharedStringsTable table = reader.getSharedStringsTable();
        //4.获取styleTable对象
        StylesTable stylesTable = reader.getStylesTable();
        //5.创建Sax的xmlReader对象
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        //6.注册事件处理器
        XSSFSheetXMLHandler xmlHandler = new XSSFSheetXMLHandler(stylesTable, table, new SheetHandler(), false);
        xmlReader.setContentHandler(xmlHandler);
        //7.逐行读取
        XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) reader.getSheetsData();
        while (sheetIterator.hasNext()){
            InputStream stream = sheetIterator.next();  //每一个sheet的流数据
            InputSource in = new InputSource(stream);
            xmlReader.parse(in);
        }
    }
}

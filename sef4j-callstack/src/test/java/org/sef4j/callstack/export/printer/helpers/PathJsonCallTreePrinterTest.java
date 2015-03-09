package org.sef4j.callstack.export.printer.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sef4j.callstack.stattree.CallTreeNode;
import org.sef4j.callstack.stattree.CallTreeNodeTstBuilder;

public class PathJsonCallTreePrinterTest {

    protected ByteArrayOutputStream buffer;
    protected PrintWriter out;
    protected PathJsonCallTreePrinter sut;
    
    @Before
    public void setup() {
        this.buffer = new ByteArrayOutputStream();
        this.out = new PrintWriter(buffer);
        PathJsonCallTreePrinter.Builder builder = new PathJsonCallTreePrinter.Builder(out);
        builder.withPropPerTypePrinter(CallTreeNodeTstBuilder.defaultPerTypePrinters(false, "{", "}")); // upcast to abstract builder!.. cannot chain method
        this.sut = builder.build();
    }
    
    @Test
    public void testRecursivePrintNodes() throws IOException {
        // Prepare
        CallTreeNode root = CallTreeNodeTstBuilder.buildTree(null, null);
        // Perform
        sut.recursivePrintNodes(root, -1);
        // Post-check
        out.flush();
        String res = buffer.toString();
        System.out.println(res);
        
        InputStream expectedStream = getClass().getResourceAsStream("PathJsonCallTreePrintTest-testRecursivePrintNodes.txt");
        String expectedRes = IOUtils.toString(expectedStream);
        Assert.assertEquals(expectedRes, res);
    }
}

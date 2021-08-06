package com.intershop.oms.ps.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bakery.persistence.dataobject.DatabaseDefinitions;

public class NukeOrders extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    public static final String QRY = "select 1 from nuke_orders();";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();

        try (Connection conny = DatabaseDefinitions.getConnection(); Statement stmt = conny.createStatement();)
        {
            out.println("Starting to nuke orders...");
            stmt.executeQuery(QRY);
            out.println("Job finished successfully.");
        }
        catch(Exception e)
        {
            out.println("ERROR: ");
            e.printStackTrace(out);
        }
        finally
        {
            out.flush();
            out.close();
        }
    }
    
}

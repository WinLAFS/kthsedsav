/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import converter.converterFacadeLocal;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Shum
 */
public class ConvertController extends HttpServlet {

    @EJB
    private converterFacadeLocal converter;
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String actionStr = request.getParameter("action");
        
        List<String> currencies = converter.getAvailableCurrencies();
        request.setAttribute("currencies", currencies);

        if(actionStr==null || actionStr.equals("")){
           request.getRequestDispatcher("converter.jsp").forward(request, response);
        } else {
            String fromCurrency = request.getParameter("fromCurrency");
            String toCurrency = request.getParameter("toCurrency");
            String amount = request.getParameter("amount");

            request.setAttribute("amount", amount);
            request.setAttribute("fromCurrency", fromCurrency);
            request.setAttribute("toCurrency", toCurrency);

            

            //TODO put logic here
            double amountDouble = 0;
            try {
                amountDouble = new Double(amount);
            }
            catch(ArithmeticException e) {
                
            }
            double result = converter.convertCurrency(fromCurrency, amountDouble, toCurrency);
            request.setAttribute("convertedAmount", result);
            request.setAttribute("message", "currency converted");

            request.getRequestDispatcher("converter.jsp").forward(request, response);
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

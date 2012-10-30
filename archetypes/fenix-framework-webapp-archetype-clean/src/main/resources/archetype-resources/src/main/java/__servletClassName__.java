package ${package};

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ${servletClassName} extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html");//seta o tipo de conteúdo da resposta da servlet para html
        PrintWriter out = response.getWriter();//instancia o printer, que vai imprimir o html.

        out.println("Bootstrap");
    }

}
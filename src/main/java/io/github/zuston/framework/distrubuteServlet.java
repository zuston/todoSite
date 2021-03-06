package io.github.zuston.framework;

import io.github.zuston.framework.core.container;
import io.github.zuston.framework.entity.handlerEntity;
import io.github.zuston.framework.entity.jsonEntity;
import io.github.zuston.framework.entity.requestEntity;
import io.github.zuston.framework.entity.viewEntity;
import io.github.zuston.framework.helper.beanHelper;
import io.github.zuston.framework.helper.configHelper;
import io.github.zuston.framework.helper.coreHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by zuston on 16/11/13.
 */
@WebServlet(urlPatterns = "/*",loadOnStartup = 0)
public class distrubuteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String viewPath = configHelper.viewPath();
        String defaultJSP = configHelper.defaultJSP();
        String requestMethod = req.getMethod().toLowerCase();
        String requestUrlPattern = req.getRequestURI().toLowerCase();
        handlerEntity handler = coreHelper.getHandler(new requestEntity(requestMethod,requestUrlPattern));
        if(handler!=null){
            Class handlerClass = handler.getHandlerClass();
            Method handlerMethod = handler.getHandlerMethod();
            Object res = null;
            try {
                res = bootstrap.reflection(beanHelper.get(handlerClass),handlerMethod, container.getAllParam(req));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if(res instanceof viewEntity){
                viewEntity view = (viewEntity)res;
                HashMap<String,Object> pageHM = view.getModel();
                String viewName = view.getView();
                req = container.putHM2Request(req,pageHM);
                req.getRequestDispatcher(viewPath+viewName).forward(req,resp);
            }

            if(res instanceof jsonEntity){
                // TODO: 16-11-28 增加json数据的处理
                jsonEntity json = (jsonEntity) res;
                HashMap<String,Object> hm = json.getModel();
                req.getRequestDispatcher(viewPath+defaultJSP).forward(req,resp);
            }
        }else{
            req.getRequestDispatcher(viewPath+defaultJSP).forward(req,resp);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        bootstrap.init();
        ServletContext servletContext = servletConfig.getServletContext();
        ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
        jspServlet.addMapping(configHelper.viewPath()+"*");
    }
}

<%@ page import="com.github.quick4j.hello.entity.Person" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=Edge">
        <title></title>
    </head>
    <body>
        <h2>Welcome Everyone!</h2>
        <ul>
            <li>
                <h3>Page Session</h3>
                <div>
                    Hello, <%= null == session.getAttribute("person")?"World" : ((Person)session.getAttribute("person")).getName()%>
                </div>
                <div>Session ID: <%=session.getId()%></div>
            </li>
            <li>
                <h3>request.getSession()</h3>
                <div>
                    Hello, <%= null == request.getSession().getAttribute("person")? "World" : ((Person)session.getAttribute("person")).getName()%>
                </div>
                <div>Session ID: <%=request.getSession().getId()%></div>
            </li>
        </ul>
    </body>
</html>

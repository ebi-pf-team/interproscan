package uk.ac.ebi.interpro.scan.jms;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.jms.JMSException;

/**
 * TODO: Add description of class.
 *
 * @author Phil Jones
 * @version $Id: SessionMethodInterceptor.java,v 1.1 2009/11/04 14:04:43 pjones Exp $
 * @since 1.0
 */
public class SessionMethodInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object result = null;
        try{
            result = methodInvocation.proceed();
        }
        catch (JMSException jmse){
            jmse.printStackTrace();
            
        }
        finally {
            //???
        }

        return result;
    }

}

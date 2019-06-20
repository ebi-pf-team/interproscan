package uk.ac.ebi.interpro.scan.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

import javax.annotation.Resource;

/**
 * This simple test checks the behaviour of the BeanNameAware interface.
 * This interface defines a single method setName(String s)
 *
 * This injects the 
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class BeanNameAwareTest {

    BeanForTesting testBean;

    @Resource
    public void setTestBean(BeanForTesting testBean) {
        this.testBean = testBean;
    }

    @Test
    public void testSetNameMethod(){
        assertTrue ("testId".equals(testBean.getId()), "The BeanNameAware interface does not set the bean id.");
    }
}

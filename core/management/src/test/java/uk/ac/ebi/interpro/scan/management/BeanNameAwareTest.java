package uk.ac.ebi.interpro.scan.management;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BeanNameAwareTest {

    BeanForTesting testBean;

    @Resource
    public void setTestBean(BeanForTesting testBean) {
        this.testBean = testBean;
    }

    @Test
    public void testSetNameMethod(){
        assertTrue ("The BeanNameAware interface does not set the bean id.", "testId".equals(testBean.getId()));
    }
}

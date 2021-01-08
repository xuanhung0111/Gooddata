package com.gooddata.qa.utils.flow;

import com.gooddata.qa.graphene.AbstractADDProcessTest;
import com.gooddata.qa.graphene.AbstractGeoPushpinTest;
import com.gooddata.qa.graphene.AbstractGreyPageTest;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class TestsRegistryTest {

    @Test
    public void test1() throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("sanity-aws", new Object[]{
                AbstractADDProcessTest.class,
                "testng-desktop-EditMode.xml"
        });

        suites.put("all", new HashMap<String, Object>() {{
            put("sanity-aws", suites.get("sanity-aws"));
            put("sanity", new Object[]{
                    AbstractProjectTest.class,
                    AbstractGeoPushpinTest.class,
                    "testng-desktop-SplashScreen.xml"
            });
        }});

        String[] args = new String[]{"all"};

        TestsRegistry.getInstance()
                .register(args, suites).toTextFile();
    }

    @Test
    public void test2() throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("sanity-aws", new Object[]{
                AbstractADDProcessTest.class,
                "testng-desktop-EditMode.xml"
        });

        suites.put("sanity", new HashMap<String, Object>() {{
            put("sanity-aws", suites.get("sanity-aws"));
            put("sanity-extend", new Object[]{
                    AbstractProjectTest.class,
                    AbstractGeoPushpinTest.class,
                    "testng-desktop-SplashScreen.xml"
            });
        }});

        suites.put("extended", new Object[]{
                AbstractGreyPageTest.class
        });

        suites.put("all", new HashMap<String, Object>() {{
            put("sanity", suites.get("sanity"));
            put("sanity-extra", new Object[]{
                    AbstractMethodError.class
            });
            put("extended", suites.get("extended"));
        }});

        String[] args = new String[]{"all"};

        TestsRegistry.getInstance()
                .register(args, suites).toTextFile();
    }


    @Test
    public void test3() throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[]{
                Screenshots.class,
                "testng-desktop-EditMode.xml",
                "testng-desktop-imap-KpiAlertEvaluate.xml"
        });

        String[] args = new String[]{"all"};

        TestsRegistry.getInstance()
                .register(args, suites).toTextFile();
    }

    @Test
    public void test4() throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new HashMap<String, Object[]>() {{
            put("sanity", new Object[]{
                    AbstractADDProcessTest.class,
                    "testng-desktop-EditMode.xml"
            });
            put("extended", new Object[]{
                    AbstractProjectTest.class,
                    AbstractGeoPushpinTest.class,
                    "testng-desktop-SplashScreen.xml"
            });
        }});

        String[] args = new String[]{"all"};

        TestsRegistry.getInstance()
                .register(args, suites).toTextFile();
    }
}

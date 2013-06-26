package com.gooddata.qa.graphene.ccc;

import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractTest;

public class AbstractCCCTest extends AbstractTest {

	@BeforeClass
	public void initStartPage() {
		startPage = "admin/dataload/";
	}
	
}

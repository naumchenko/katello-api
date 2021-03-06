package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;

import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.tngext.TngPriority;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;

@TngPriority(900)
@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class CompositeContentViewTests extends KatelloCliTestBase{
	
	String uid = KatelloUtils.getUniqueID();
	String del_changeset_name = "del_changeset-" + uid;
	String contDefName1 = "condef1-" + uid;
	String contDefName2 = "condef2-" + uid;
	String contDefName3 = "condef3-" + uid;
	String cdCompositeName = "condefcomposite-" + uid;
	String cdCompositeName2 = "condefcomposite2-" + uid;
	String contViewName1_1 = "pubview1-1" + uid;
	String contViewName1_2 = "pubview1-2" + uid;
	String contViewName2_1 = "pubview2-1" + uid;
	String contViewName2_2 = "pubview2-2" + uid;
	String contViewName3_1 = "pubview3-1" + uid;
	String contViewName3_2 = "pubview3-2" + uid;
	String pubcompview_name1 = "pubcompview1" + uid;
	String pubcompview_name2 = "pubcompview2" + uid;
	String act_key_name2 = "act_key2" + uid;
	String system_name2 = "system2" + uid;

	KatelloChangeset del_changeset;
	KatelloContentDefinition _contDef1;
	KatelloContentDefinition _contDef2;
	KatelloContentDefinition _contDef3;
	KatelloContentDefinition compositeContentDef;
	KatelloContentDefinition compcondef2;
	KatelloContentView compconview;
	KatelloContentView compconview2;
	KatelloContentView conview1;
	KatelloContentView conview2;
	KatelloContentView conview3_1;
	KatelloContentView conview3_2;
	KatelloActivationKey act_key2;
	KatelloSystem sys2;
	
	@Test(description="initialization here")
	public void init(){
		// create content definition from zoo repo
		_contDef1 = new KatelloContentDefinition(cli_worker, contDefName1,null, base_org_name, null);
		exec_result = _contDef1.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = _contDef1.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		// publish content view zoo - version1
		exec_result = _contDef1.publish(contViewName1_1, contViewName1_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// publish content view zoo - version2
		exec_result = _contDef1.publish(contViewName1_2, contViewName1_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// create content definition from zoo4 repo
		_contDef2 = new KatelloContentDefinition(cli_worker, contDefName2,null,base_org_name,null);
		exec_result = _contDef2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = _contDef2.add_repo(base_zoo4_product_name, base_zoo4_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		// publish content view zoo4 - version1
		exec_result = _contDef2.publish(contViewName2_1, contViewName2_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// publish content view zoo4 - version2
		exec_result = _contDef2.publish(contViewName2_2, contViewName2_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		// promote both content views' version2 to the DEV environment
		conview1 = new KatelloContentView(cli_worker, contViewName1_2, base_org_name);
		exec_result = conview1.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		conview2 = new KatelloContentView(cli_worker, contViewName2_2, base_org_name);
		exec_result = conview2.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// Empty content definition ??? why?
		_contDef3 = new KatelloContentDefinition(cli_worker, contDefName3,null, base_org_name, null);
		exec_result = _contDef3.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// publish content view - version1
		exec_result = _contDef3.publish(contViewName3_1, contViewName3_1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		// publish content view - version2
		exec_result = _contDef3.publish(contViewName3_2, contViewName3_2, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
			
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"service rhsmcertd restart");
		yum_clean(cli_worker.getClientHostname());
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"service goferd restart;");
	}
	
	@Test(description="Create composite content view definition", dependsOnMethods={"init"})
	public void test_createComposite() {
		compositeContentDef = new KatelloContentDefinition(cli_worker, cdCompositeName, null, base_org_name, null);
		exec_result = compositeContentDef.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		// add zoo content view version2
		exec_result = compositeContentDef.add_view(contViewName1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		// add zoo4 content view version2
		exec_result = compositeContentDef.add_view(contViewName2_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description="Check adding old views (version 1) into composite content view definition", dependsOnMethods={"test_createComposite"})
	public void test_checkOldViewsIntoComposite() {
		exec_result = compositeContentDef.add_view(contViewName1_1);
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(KatelloContentDefinition.ERR_ADDVIEW), "Error in adding older view");
		
		exec_result = compositeContentDef.add_view(contViewName2_1);
		Assert.assertTrue(exec_result.getExitCode() == 144, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(KatelloContentDefinition.ERR_ADDVIEW), "Error in adding older view");
	}

	@Test(description="add/remove views into composite content view definition", dependsOnMethods={"test_checkOldViewsIntoComposite"})
	public void test_addRemoveViewsIntoComposite() {
		exec_result = compositeContentDef.remove_view(contViewName1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compositeContentDef.info();
		Assert.assertFalse(getOutput(exec_result).contains(contViewName1_2), "Not contains view");
		Assert.assertTrue(getOutput(exec_result).contains(contViewName2_2), "Contains view");
		
		exec_result = compositeContentDef.remove_view(contViewName2_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compositeContentDef.info();
		Assert.assertFalse(getOutput(exec_result).contains(contViewName1_2), "Not contains view");
		Assert.assertFalse(getOutput(exec_result).contains(contViewName2_2), "Not contains view");
		
		exec_result = compositeContentDef.remove_view(contViewName2_2);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (Cannot remove when not a component)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.ERR_NOT_A_COMPONENT, contViewName2_2, compositeContentDef.name)), "Check output (not a component)");

		exec_result = compositeContentDef.add_view(contViewName1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	

		exec_result = compositeContentDef.info();
		Assert.assertTrue(getOutput(exec_result).contains(contViewName1_2), "Contains view");
		Assert.assertFalse(getOutput(exec_result).contains(contViewName2_2), "Not contains view");
		
		exec_result = compositeContentDef.add_view(contViewName2_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compositeContentDef.info();
		Assert.assertTrue(getOutput(exec_result).contains(contViewName1_2), "Contains view");
		Assert.assertTrue(getOutput(exec_result).contains(contViewName2_2), "Contains view");
		// at the end: both version2 of zoo and zoo4 are back in composite content def.
	}

	
	@Test(description="Consume content from composite content view definition", dependsOnMethods={"test_addRemoveViewsIntoComposite"})
	public void test_consumeCompositeContent() {
		// erase packages
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum erase -y wolf lion crab walrus shark cheetah");
		
		exec_result = compositeContentDef.publish(pubcompview_name1, pubcompview_name1, "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		
		compconview = new KatelloContentView(cli_worker, pubcompview_name1, base_org_name);
		exec_result = compconview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubcompview_name1, base_dev_env_name)), "Content view promote output.");
		
		act_key2 = new KatelloActivationKey(cli_worker, base_org_name, base_dev_env_name, act_key_name2, "Act key2 created", null, pubcompview_name1);
		exec_result = act_key2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key2.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubcompview_name1), "Content view name is in output.");
		
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),KatelloSystem.RHSM_CLEAN);
		sys2 = new KatelloSystem(cli_worker, system_name2, base_org_name, null);
		exec_result = sys2.rhsm_registerForce(act_key_name2);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		exec_result = sys2.subscribe(base_zoo_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys2.subscribe(base_pulp_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");

		exec_result = sys2.subscribe(base_zoo4_repo_pool);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(), "subscription-manager refresh; service rhsmcertd restart");
		yum_clean(cli_worker.getClientHostname());
		
		//install packages from content view 1 and 2
		install_Packages(cli_worker.getClientHostname(),new String[] {"lion", "crab"});
		
		//package should not be available to install
		exec_result = KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum install pulp-agent --disablerepo '*pulp*'");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No package pulp-agent available."));
	}

	@Test(description="remove view from content definition", dependsOnMethods={"test_consumeCompositeContent"})
	public void test_removeViewFromDefinition()
	{
		//Remove published view (not a component) - verify error
		exec_result = compositeContentDef.remove_view(pubcompview_name1);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (Cannot remove when not a component)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.ERR_NOT_A_COMPONENT, pubcompview_name1, compositeContentDef.name)), "Check output (not a component)");

		exec_result = compositeContentDef.add_view(contViewName3_2);
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (add component view)");
		exec_result = compositeContentDef.remove_view(contViewName3_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check exit code (remove view)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.OUT_REMOVE_VIEW, contViewName3_2, compositeContentDef.name)), "Check output (remove component)");

		exec_result = compositeContentDef.info();
		String viewsList = KatelloUtils.grepCLIOutput("Component Views", getOutput(exec_result).trim());
		Assert.assertFalse(viewsList.contains(contViewName3_2), "Check view name present");
		//Try to remove an existing View but not associated with the definition - verify error
		exec_result = _contDef2.publish("new-view", "new_view", "Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = compositeContentDef.remove_view("new-view");
		Assert.assertTrue(exec_result.getExitCode()==0, "Check exit code (Cannot remove when not a component)");
		Assert.assertTrue(getOutput(exec_result).equals(String.format(KatelloContentDefinition.ERR_NOT_A_COMPONENT, "new-view", compositeContentDef.name)), "Check output (not a component)");
	} 
	
	@Test(description = "part of promoted composite content view delete by changeset from environment, then repromote composite view, verify that packages are still availble",
			dependsOnMethods={"test_removeViewFromDefinition"})
	public void test_deletePromotedContentViewPart() {
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum erase -y zebra");
		
		del_changeset = new KatelloChangeset(cli_worker, del_changeset_name, base_org_name, base_dev_env_name, true);
		exec_result = del_changeset.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = del_changeset.update_addView(contViewName1_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = del_changeset.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		install_Packages(cli_worker.getClientHostname(),new String[] {"zebra"});
	}

	@Test(description = "removed content view on previous scenario promote back by changeset to environment, verify that packages are availble",
			dependsOnMethods={"test_deletePromotedContentViewPart"})
	public void test_RePromoteContentViewPart() {
		KatelloUtils.sshOnClient(cli_worker.getClientHostname(),"yum erase -y tiger");
		
		compconview = new KatelloContentView(cli_worker, contViewName1_2, base_org_name);
		exec_result = compconview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(
				KatelloContentView.OUT_PROMOTE, this.contViewName1_2, base_dev_env_name)), 
				"Content view promote output.");
		install_Packages(cli_worker.getClientHostname(),new String [] {"tiger"});
	}	
	
	@Test(description="Create composite content view definition, add content views, add repos into them, refresh views and try to publish composite view", dependsOnMethods={"init","test_RePromoteContentViewPart"})
	public void test_publishCompositeFail() {
		compcondef2 = new KatelloContentDefinition(cli_worker, cdCompositeName2, null, base_org_name, null);
		exec_result = compcondef2.create(true);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef2.add_view(contViewName3_1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = compcondef2.add_view(contViewName3_2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = _contDef3.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		conview3_1 = new KatelloContentView(cli_worker, contViewName3_1, base_org_name);
		exec_result = conview3_1.refresh_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		conview3_2 = new KatelloContentView(cli_worker, contViewName3_2, base_org_name);
		exec_result = conview3_2.refresh_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = compcondef2.publish(pubcompview_name2, pubcompview_name2, null);
		Assert.assertFalse(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Cannot publish definition. Please check for repository conflicts"), "Error message in publishing");
	}
}

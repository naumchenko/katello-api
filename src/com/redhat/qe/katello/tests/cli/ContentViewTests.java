package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloChangeset;
import com.redhat.qe.katello.base.obj.KatelloContentDefinition;
import com.redhat.qe.katello.base.obj.KatelloContentView;
import com.redhat.qe.katello.base.obj.KatelloErrata;
import com.redhat.qe.katello.base.obj.KatelloPackage;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.common.TngRunGroups;
import com.redhat.qe.katello.tests.e2e.PromoteErrata;

@Test(groups=TngRunGroups.TNG_KATELLO_Content)
public class ContentViewTests extends KatelloCliTestBase{
	
	public static final String ERRATA_ZOO_SEA = "RHEA-2012:0002";
	
	String uid = KatelloUtils.getUniqueID();
	String changeset_name2 = "changecon2-" + uid;
	String condef_name = "condef-" + uid;
	String conview_name = "conview-" + uid;
	String pubview_name = "pubview-" + uid;
	String condef_name1 = "condef1-" + uid;
	String condef_name2 = "condef2-" + uid;
	String act_key_name = "act_key" + uid;
	String group_name = "group" + uid;
	String system_name1 = "system" + uid;
	String system_uuid1;
	
	KatelloChangeset changeset2;
	KatelloContentDefinition condef;
	KatelloContentView conview;
	KatelloActivationKey act_key;
	KatelloSystem sys;
	KatelloSystemGroup group;
	
	@Test(description="initialization goes here")
	public void init(){		
		condef = new KatelloContentDefinition(cli_worker, condef_name,null,base_org_name,null);
		exec_result = condef.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = condef.add_repo(base_zoo_product_name, base_zoo_repo_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		
		exec_result = condef.publish(pubview_name,pubview_name,"Publish Content");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
	
		
		// The only way to install ERRATA on System is by system group
		group = new KatelloSystemGroup(this.cli_worker, group_name, base_org_name);
		exec_result = group.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		sshOnClient("sed -i -e \"s/certFrequency.*/certFrequency = 1/\" /etc/rhsm/rhsm.conf");
		sshOnClient("service rhsmcertd restart");
		yum_clean();
		sshOnClient("service goferd restart;");
	}

	@Test(description="promote content view to environment",groups={"cfse-cli"}, dependsOnMethods={"init"})
	public void test_promoteContentView() {
		conview = new KatelloContentView(cli_worker, pubview_name, base_org_name);
		exec_result = conview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_dev_env_name)), "Content view promote output.");
	}
	
	@Test(description = "Adding a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_promoteContentView"})
	public void test_addContentView() {
		
		act_key = new KatelloActivationKey(this.cli_worker, base_org_name,base_dev_env_name,act_key_name,"Act key created");
		exec_result = act_key.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
	}
	
	@Test(description = "Remove a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_addContentView"})
	public void test_removeContentViewFromKey() {
		
		exec_result = act_key.update_remove_content_view();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertFalse(getOutput(exec_result).contains(this.pubview_name), "Content view name is not in output.");
	}

	@Test(description = "Re Add a published content view to an activation key",groups={"cfse-cli"}, dependsOnMethods={"test_removeContentViewFromKey"})
	public void test_reAddContentViewToKey() {
		
		exec_result = act_key.update_add_content_view(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		exec_result = act_key.info();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");	
		Assert.assertTrue(getOutput(exec_result).contains(this.pubview_name), "Content view name is in output.");
	}
	
	@Test(description = "register client via activation key",groups={"cfse-cli"}, dependsOnMethods={"test_reAddContentViewToKey"})
	public void test_registerClient(){
		sshOnClient(KatelloSystem.RHSM_CLEAN);
		sys = new KatelloSystem(this.cli_worker, system_name1, base_org_name, null);
		exec_result = sys.rhsm_registerForce(act_key_name);
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		
		exec_result = sys.rhsm_identity();
		system_uuid1 = KatelloUtils.grepCLIOutput("Current identity is", exec_result.getStdout());
		
		exec_result = sys.subscriptions_available();
		String poolId1 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),1);
		Assert.assertNotNull(poolId1, "Check - pool Id is not null");
		
		String poolId2 = KatelloUtils.grepCLIOutput("ID", getOutput(exec_result).trim(),2);
		Assert.assertNotNull(poolId2, "Check - pool Id is not null");
		
		exec_result = sys.subscribe(poolId1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = sys.subscribe(poolId2);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
	}
	
	@Test(description = "List the packages of content view",groups={"cfse-cli"}, dependsOnMethods={"test_registerClient"})
	public void test_packageList() {
		KatelloPackage pack = new KatelloPackage(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, pubview_name);
		
		exec_result = pack.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains("lion"),"is package in the list: zebra");
		Assert.assertTrue(getOutput(exec_result).contains("wolf"),"is package in the list: wolf");
		Assert.assertTrue(getOutput(exec_result).contains("zebra"),"is package in the list: zebra");
	}
	
	@Test(description = "Consuming content using an activation key that has a content view definition",groups={"cfse-cli"}, dependsOnMethods={"test_packageList"})
	public void test_consumeContent()
	{
		sshOnClient("yum erase -y lion");
		yum_clean();
		install_Packages(cli_worker.getClientHostname(), new String[] {"lion"});
		
		// chack that packages from other repos not in content view are not available
		exec_result = sshOnClient("yum install pulp-agent --disablerepo '*pulp*'");
		Assert.assertTrue(getOutput(exec_result).trim().contains("No package pulp-agent available."));
	}
	
	@Test(description = "List the erratas of content view",groups={"cfse-cli"}, dependsOnMethods={"test_consumeContent"})
	public void test_errataList() {
		KatelloErrata errata = new KatelloErrata(cli_worker, base_org_name, base_zoo_product_name, base_zoo_repo_name, pubview_name);
		
		exec_result = errata.cli_list();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		Assert.assertTrue(getOutput(exec_result).contains(ERRATA_ZOO_SEA),"is package in the list: " + ERRATA_ZOO_SEA);
	}
	
	@Test(description = "consume Errata content",groups={"cfse-cli"}, dependsOnMethods={"test_errataList"})
	public void test_ConsumeErrata(){
		sshOnClient("yum erase -y walrus");
		exec_result = sshOnClient("yum install -y walrus-0.71-1.noarch");
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		KatelloErrata ert = new KatelloErrata(cli_worker, ERRATA_ZOO_SEA, base_org_name, base_zoo_product_name, base_zoo_repo_name, null);
		ert.content_view = pubview_name;
		exec_result = ert.info();
		Assert.assertTrue(exec_result.getExitCode().intValue()==0, "Check - return code (errata info --environment Dev)");
		
		exec_result = group.add_systems(system_uuid1);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		
		exec_result = group.erratas_install(PromoteErrata.ERRATA_ZOO_SEA);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).trim().contains("Remote action finished"));
		Assert.assertTrue(getOutput(exec_result).trim().contains("Erratum Install Complete"));
	}

	@Test(description = "promoted content view delete by changeset from environment, " +
			"verify that packages are not availble anymore",groups={"cfse-cli"}, dependsOnMethods={"test_ConsumeErrata"})
	public void test_deletePromotedContentView() {
		sshOnClient("yum erase -y walrus");
		
		changeset2 = new KatelloChangeset(cli_worker, changeset_name2,base_org_name,base_dev_env_name, true);
		exec_result = changeset2.create();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		exec_result = changeset2.update_addView(pubview_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");		
		exec_result = changeset2.apply();
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		yum_clean();
		
		verify_PackagesNotAvailable(cli_worker.getClientHostname(), new String[] {"walrus"});
	}

	//@ TODO bug 956690
	@Test(description = "removed content view on previous scenario promote back by changeset to environment, verify that packages are already availble",
			groups={"cfse-cli"}, dependsOnMethods={"test_deletePromotedContentView"})
	public void test_RePromoteContentView() {
		sshOnClient("yum erase -y walrus");
		
		exec_result = conview.promote_view(base_dev_env_name);
		Assert.assertTrue(exec_result.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(exec_result).contains(String.format(KatelloContentView.OUT_PROMOTE, this.pubview_name, base_dev_env_name)), "Content view promote output.");
		yum_clean();
		install_Packages(cli_worker.getClientHostname(), new String[] {"walrus"});
	}
}

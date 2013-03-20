package com.redhat.qe.katello.tests.cli;
import java.io.File;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SCPTools;
@Test(groups={"headpin-cli"})
public class ContentTest extends KatelloCliTestScript{

	private SSHCommandResult res;
	private String org_name;
	private String env_name;
	private KatelloOrg org;
	private KatelloEnvironment env;

	private String manifest = "sam-1RHEL6Server.zip";

	@BeforeClass(description="init: create initial stuff", alwaysRun=true)
	public void setUp()
	{
		String uid = KatelloUtils.getUniqueID();
		org_name = "org-content" + uid;
		env_name = "env-content" + uid;
		org = new KatelloOrg(this.org_name,"Org-content Created");
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		env = new KatelloEnvironment(this.env_name, "Env-content Created",this.org_name,KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode() == 0 , "Check - return code");
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "null"));
		Assert.assertTrue(scp.sendFile("data"+File.separator+manifest, "/tmp"),
				manifest+" sent successfully");		

	}
	
	@Test(description="import a manifest that contains RHEL6 data.")
	public void test_importManifest(){
		KatelloProvider redhat = new KatelloProvider(KatelloProvider.PROVIDER_REDHAT, org.getName(), null, null);
		SSHCommandResult res = redhat.import_manifest("/tmp/"+manifest, true);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - manifest imported successfully.");
	}

	@Test(description = "Content test check whether the registered consumer is " +
			"able to access repo and obtain contents", dependsOnMethods={"test_importManifest"})
	public void test_Content()
	{
		res = KatelloUtils.sshOnClient("rpm -qa | grep yum-utils");
		int exitCode = res.getExitCode().intValue();
		if(exitCode == 1)
		{
			res = KatelloUtils.sshOnClient("yum install -y yum-utils");
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");

		}
		res = KatelloUtils.sshOnClient("yum-config-manager --enable beaker-HighAvailability beaker-LoadBalancer beaker-ResilientStorage beaker-ScalableFileSystem beaker-Server beaker-debuginfo beaker-harness beaker-optional beaker-tasks");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = KatelloUtils.sshOnClient("subscription-manager register --user admin --password admin --org "+ this.org_name +" --environment "+this.env_name +" --force");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = KatelloUtils.sshOnClient("yum repolist");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = KatelloUtils.sshOnClient("yum install -y zsh");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}

	@AfterClass(description="remove the org, let manifest be reused.", alwaysRun=true)
	public void tearDown(){
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode() == 0 , "Check - return code (org removed)");
	}
}

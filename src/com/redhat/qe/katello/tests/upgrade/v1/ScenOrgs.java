package com.redhat.qe.katello.tests.upgrade.v1;

import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloCliTestBase;
import com.redhat.qe.katello.base.obj.KatelloActivationKey;
import com.redhat.qe.katello.base.obj.KatelloDistributor;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloGpgKey;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPermission;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloSystemGroup;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.base.obj.KatelloUserRole;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

@Test(groups={"sam-upgrade"})
public class ScenOrgs implements KatelloConstants {
	
	protected static Logger log = Logger.getLogger(ScenOrgs.class.getName());
	
	String _env_1;
	String _env_2;
	String _env_3;
	String _admin_env;
	String _perm_1;
	String _perm_2;
	String _perm_3;
	String _admin_perm;
	String _del_system;
	String _del_system2;
	String _system;
	String _system2;
	String _system3;
	String _system_group;
	String _system_group2;
	String _system_group3;
	String _distributor_name;
	String _distributor_name2;
	String _org;
	String _admin_org;
	String _user;
	String _admin_user;
	String _admin_user2;
	String _akey;
	String _akey2;
	String _akey3;
	String _user_role;
	String _user_role2;
	String _user_role3;
	String _admin_role;
	String _neworg;
	String _newuser;
	String _newsystem;
	String _poolRhel;
	String _poolRhel2;
	String _poolRhel3;
	String _keyname1;
	String _keyname2;
	String _admin_key;
	String[] _act_key = new String[2];
	String[] _org_act_key = new String[2];
	String[] _env_act_key = new String[2];
	String[] _sys_act_key = new String[2];
	String[] _sys_act_key_ID = new String[2];
	String[] _org_default_info= new String[2];
	String[] _keyname= new String[2];
	String[] _permorg = new String[3];
	String[] _permrole = new String[3];
	String[] _perm = new String[3];
	String[] _del_org= new String[3];
	String[] _multorg =  new String[5];
	String[] _multuser = new String[5];
	String[] _multuser_role = new String[5];
	String _user_default_org;
	String _org_default;
	String _role_default;
	KatelloOrg orgDefault;
	KatelloUser userOrgDefault;
	KatelloUserRole userRoleAssign;
	int _init_role_count;

	@Test(description="init object unique names", 
			groups={TNG_PRE_UPGRADE})
	public void init(){
		String _uid = KatelloUtils.getUniqueID();
		_org = "torg"+_uid;
		_admin_org = "adminorg"+_uid;
		_user = "tuser"+_uid;
		_newuser = "newuser" + _uid;
		_admin_user = "adminuser1" + _uid;
		_admin_user2 = "adminuser2" + _uid;
		_multuser[0] = "mult-user0-"+ _uid;
		_multuser[1] = "mult-user1-"+ _uid;
		_multuser[2] = "mult-user2-"+ _uid;
		_multuser[3] = "mult-user3-"+ _uid;
		_multuser[4] = "mult-user4-"+ _uid;
		_user_default_org = "user-org-default" + _uid;
		String ldap_type = System.getProperty("ldap.server.type", "");		
		if ("posix".equals(ldap_type)) {
			_user = "gszasz";
			_newuser = "sloranz";
			_admin_user = "apagac";
			_admin_user2 = "kwhitney";
			_multuser[0] = "jlaska";
			_multuser[1] = "imcleod";
			_multuser[2] = "psharma";
			_multuser[3] = "rananda";
			_multuser[4] = "rlandy";
			_user_default_org = "wadkins";
		} else if ("free_ipa".equals(ldap_type) || "active_directory".equals(ldap_type)) {
			_user = "admin-user2";
			_newuser = "admin-user3";
		}
		
		_akey = "akey"+_uid;
		_akey2 = "akey2"+_uid;
		_akey3 = "akey3"+_uid;
		_keyname1 = "testkey1"+_uid;
		_keyname2 = "testkey2"+_uid;
		_admin_key = "adminkey"+_uid;
		_neworg = "neworg"+_uid;
		_user_role = "role" + _uid;
		_user_role2 = "role2" + _uid;
		_user_role3 = "role3" + _uid;
		_admin_role = "adminrole" + _uid;
		_env_1 = "Dev_" + _uid;
		_env_2 = "QA_" + _uid;
		_env_3 = "GA_" + _uid;
		_admin_env = "testenv" + _uid;
		_perm_1 = "Perm1_" + _uid;
		_perm_2 = "Perm2_" + _uid;
		_perm_3 = "Perm3_" + _uid;
		_admin_perm = "perm" + _uid;
		_del_system = "deletable" + _uid;
		_del_system2 = "deletable2" + _uid;
		_system = "Dakar_" + _uid;
		_system2 = "Paris_" + _uid;
		_system3 = "Madrid_" + _uid;
		_system_group = "sysgroup" + _uid;
		_system_group2 = "sysgroup2" + _uid;
		_system_group3 = "sysgroup3" + _uid;
		_distributor_name = "distro" + _uid;
		_distributor_name2 = "distro2" + _uid;
		_newsystem = "newsystem" + _uid;
	}
	
	@Test(description="prepare all test data, org, environment, activation key and role", 
			dependsOnMethods={"init"}, 
			groups={TNG_PRE_UPGRADE})
	public void createData(){
		KatelloUtils.sshOnClient(null, KatelloSystem.RHSM_CLEAN);
		KatelloUtils.sshOnClient(null, "rpm -e "+KatelloGpgKey.GPG_PUBKEY_RPM_ZOO+" || true");

		KatelloOrg org = new KatelloOrg(null, _org, null);
		KatelloEnvironment env1 = new KatelloEnvironment(null, _env_1, null, _org, KatelloEnvironment.LIBRARY);
		KatelloEnvironment env2 = new KatelloEnvironment(null, _env_2, null, _org, _env_1);
		KatelloEnvironment env3 = new KatelloEnvironment(null, _env_3, null, _org, _env_2);
		
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = env1.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = env2.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = env3.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		KatelloUser user = new KatelloUser(null, _user, 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		user = new KatelloUser(null, _admin_user2, 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = user.assign_role(KatelloUserRole.ROLE_ADMINISTRATOR);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		
		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org, _env_1, _akey, null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		key = new KatelloActivationKey(null, 
				_org, _env_2, _akey2, null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		key = new KatelloActivationKey(null, 
				_org, _env_3, _akey3, null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		_init_role_count = Integer.parseInt(new KatelloUserRole(null, null, null).cli_list_count().getStdout().trim());
		
		KatelloUserRole user_role = new KatelloUserRole(null, _user_role, "Environments");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		_init_role_count++;
		
		user_role = new KatelloUserRole(null, _user_role2, "Activation Keys");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");	
		_init_role_count++;
		
		user_role = new KatelloUserRole(null, _user_role3, "Roles");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");	
		_init_role_count++;
		
		KatelloPermission perm = new KatelloPermission(null, _perm_1, _org, "environments", null, "read_contents,update_systems", _user_role);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		perm = new KatelloPermission(null, _perm_2, _org, "activation_keys", null, "manage_all", _user_role2);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		perm = new KatelloPermission(null, _perm_3, _org, "roles", null, "delete,read", _user_role3);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, "/tmp");

		KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _org, null, null);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_12SUBSCRIPTIONS, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		org = new KatelloOrg(null, _org, null);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		// getting poolid could vary - might be need to make switch case here for different versions...
		_poolRhel = KatelloUtils.grepCLIOutput("ID", KatelloCliTestBase.sgetOutput(res));
		if (_poolRhel == null || _poolRhel.isEmpty()) {
			_poolRhel = KatelloUtils.grepCLIOutput("Id", KatelloCliTestBase.sgetOutput(res));
		}
		
		KatelloSystem sys = new KatelloSystem(null, _del_system, _org, _env_1);
		sys.runOn(SetupServers.client_name);
		KatelloUtils.sshOnClient(SetupServers.client_name, "subscription-manager clean");
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register");
		res = sys.rhsm_identity();
		String uuid1 = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		sys.rhsm_unregister();
		
		sys = new KatelloSystem(null, _del_system2, _org, _env_2);
		sys.runOn(SetupServers.client_name2);
		KatelloUtils.sshOnClient(SetupServers.client_name2, "subscription-manager clean");
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register");
		res = sys.rhsm_identity();
		String uuid2 = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		sys.rhsm_unregister();
		
		sys.setUuid(uuid1);
		sys.runOn(null);
		res = sys.remove();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - system remove");
		
		sys.setUuid(uuid2);
		sys.runOn(null);
		res = sys.remove();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - system remove");
		
		sys = new KatelloSystem(null, _system, _org, _env_1);
		sys.runOn(SetupServers.client_name);
		KatelloUtils.sshOnClient(SetupServers.client_name, "subscription-manager clean");
		res = sys.rhsm_registerForce(_akey);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		res = sys.rhsm_subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
		
		sys = new KatelloSystem(null, _system2, _org, _env_2);
		sys.runOn(SetupServers.client_name2);
		KatelloUtils.sshOnClient(SetupServers.client_name2, "subscription-manager clean");
		res = sys.rhsm_registerForce(_akey2);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		res = sys.rhsm_subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
		
		sys = new KatelloSystem(null, _system3, _org, _env_3);
		sys.runOn(SetupServers.client_name3);
		KatelloUtils.sshOnClient(SetupServers.client_name3, "subscription-manager clean");
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		res = sys.rhsm_subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
	}
	
	@Test(description="Multiple Orgs - Delete some of them", 
			dependsOnMethods={"init"},
			groups={TNG_PRE_UPGRADE})
	public void addMultipleOrgs(){
		String uid = KatelloUtils.getUniqueID(); 
		_multorg[0] = "mult-org0-"+ uid;
		KatelloOrg org = new KatelloOrg(null,_multorg[0], null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		_multorg[1] = "mult-org1-"+ uid;
		org = new KatelloOrg(null,_multorg[1],null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		_multorg[2] = "mult-org2" + uid;
		org = new KatelloOrg(null,_multorg[2],null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		_multorg[3] = "mult-org3" + uid;
		org = new KatelloOrg(null,_multorg[3],null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		_multorg[4] = "mult-org4" + uid;
		org = new KatelloOrg(null,_multorg[4],null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
	}

	@Test(description="verify multiply orgs survived the upgrade - delete some of them", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkMultOrgsSurvived(){
		// Check the non-existence of deleted orgs
		KatelloOrg org = new KatelloOrg(null, _multorg[0], null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==148, "Check - exit code (org info)");
		org = new KatelloOrg(null, _multorg[1], null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==148, "Check - exit code (org info)");
		org = new KatelloOrg(null, _multorg[2], null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");

		//Deleting some of the orgs
		org = new KatelloOrg(null, _multorg[3], null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org delete)");

		org = new KatelloOrg(null, _multorg[4], null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org delete)");		
	}

	@Test(description="Multiple Users - Delete some of them",
			dependsOnMethods={"init"},
			groups={TNG_PRE_UPGRADE})
	public void addMultipleUsers(){ 
		KatelloUser user = new KatelloUser(null, _multuser[0], 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		SSHCommandResult res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = user.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		user = new KatelloUser(null, _multuser[1], 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = user.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		user = new KatelloUser(null, _multuser[2], 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		user = new KatelloUser(null, _multuser[3], 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		user = new KatelloUser(null, _multuser[4], 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");	
	}

	@Test(description="verify multiply users survived the upgrade - delete some of them", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkMultUsersSurvived(){
		KatelloUser user = new KatelloUser(null, _multuser[0], 
				null, null, false);
		SSHCommandResult res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==65, "Check - exit code (user info)");

		user = new KatelloUser(null, _multuser[1], 
				null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==65, "Check - exit code (user info)");

		user = new KatelloUser(null, _multuser[2], 
				null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");

		// Delete some of the users

		user = new KatelloUser(null, _multuser[3], 
				null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		res = user.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user delete)");

		user = new KatelloUser(null, _multuser[4], 
				null, null, false);
		res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
		res = user.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user delete)");

	}

	@Test(description="Multiple UserRole - Delete some of them", 
			dependsOnMethods={"init"},
			groups={TNG_PRE_UPGRADE})
	public void addMultipleUserRole(){
		String uid = KatelloUtils.getUniqueID(); 
		_multuser_role[0] = "mult-user_role0-"+ uid;
		KatelloUserRole user_role = new KatelloUserRole(null, _multuser_role[0], "Multiple User Roles");
		SSHCommandResult res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		_init_role_count++;

		_multuser_role[1] = "mult-user_role1-"+ uid;
		user_role = new KatelloUserRole(null, _multuser_role[1], "Multiple User Roles");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		_init_role_count++;
		
		_multuser_role[2] = "mult-user_role2-"+ uid;
		user_role = new KatelloUserRole(null, _multuser_role[2], "Multiple User Roles");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		_init_role_count++;
		
		_multuser_role[3] = "mult-user_role3-"+ uid;
		user_role = new KatelloUserRole(null, _multuser_role[3], "Multiple User Roles");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		res = user_role.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role delete)");

		_multuser_role[4] = "mult-user_role4-"+ uid;
		user_role = new KatelloUserRole(null, _multuser_role[4], "Multiple User Roles");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		res = user_role.cli_delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role delete)");

	}

	@Test(description="verify multiply user_roles survived the upgrade - delete some of them", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkMultUserRoleSurvived(){

		// Delete some of the user roles created
		KatelloUserRole user_role = new KatelloUserRole(null, _multuser_role[0], null);
		SSHCommandResult res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_multuser_role[0]), "Role name is in info output");
		res = user_role.cli_delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role delete)");
		_init_role_count--;
		
		user_role = new KatelloUserRole(null, _multuser_role[1], null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_multuser_role[1]), "Role name is in info output");
		res = user_role.cli_delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role delete)");
		_init_role_count--;
		
		user_role = new KatelloUserRole(null, _multuser_role[2], null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_multuser_role[2]), "Role name is in info output");

		//Check non-existing deleted user roles
		user_role = new KatelloUserRole(null, _multuser_role[3], null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==65, "Check - exit code (role info)");
		Assert.assertFalse(res.getStdout().contains(_multuser_role[3]), "Role does not exist");

		user_role = new KatelloUserRole(null, _multuser_role[4], null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==65, "Check - exit code (role info)");
		Assert.assertFalse(res.getStdout().contains(_multuser_role[4]), "Role does not exist");
	}

	@Test(description="Create admin user, perform some actions and delete user",
			dependsOnMethods={"init"},
			groups={TNG_PRE_UPGRADE})
	public void deleteAdminUser(){ 
		KatelloUser user = new KatelloUser(null, _admin_user, 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		SSHCommandResult res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		res = user.assign_role(KatelloUserRole.ROLE_ADMINISTRATOR);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		KatelloOrg org = new KatelloOrg(null, _admin_org, null);
		org.runAs(user);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		user.runAs(user);
		res = user.update_defaultOrg(_admin_org);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		KatelloEnvironment env = new KatelloEnvironment(null, _admin_env, null, _admin_org, KatelloEnvironment.LIBRARY);
		env.runAs(user);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		KatelloActivationKey key = new KatelloActivationKey(null, 
				_admin_org, _admin_env, _admin_key, null, null);
		key.runAs(user);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		
		KatelloUtils.scpOnClient(null, "data/manifest-multy.zip", "/tmp");
		KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _admin_org, null, null);
		rh.runAs(user);
		res = rh.import_manifest("/tmp/manifest-multy.zip", null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");

		KatelloUserRole user_role = new KatelloUserRole(null, _admin_role, "Activation Keys");
		user_role.runAs(user);
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");	
		_init_role_count++;
		
		KatelloPermission perm = new KatelloPermission(null, _admin_perm, _admin_org, "activation_keys", null, "manage_all", _admin_role);
		perm.runAs(user);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permition create)");
		
		user.runAs(null);
		res = user.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code - user delete");
	}
	
	@Test(description="Multiple Permission - Delete some of them", 
			dependsOnMethods={"init"},
			groups={TNG_PRE_UPGRADE})
	public void addMultiplePermissions(){

		String uid = KatelloUtils.getUniqueID(); 
		_permorg[0] = "perm-org0-"+ uid;
		KatelloOrg org = new KatelloOrg(null,_permorg[0], null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		_permrole[0] = "perm-user_role0-"+ uid;
		KatelloUserRole user_role = new KatelloUserRole(null, _permrole[0], "User Roles to add Permissions");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		_init_role_count++;
		
		_perm[0] = "perm0-"+uid;
		KatelloPermission perm = new KatelloPermission(null, _perm[0], _permorg[0], "users", null, "create, delete, update, read",_permrole[0]);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permission create)");

		_permorg[1] = "perm-org1-"+ uid;
		org = new KatelloOrg(null,_permorg[1], null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		_permrole[1] = "perm-user_role1-"+ uid;
		user_role = new KatelloUserRole(null, _permrole[1], "User Roles to add Permissions");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		_init_role_count++;
		
		_perm[1] = "perm1-"+uid;
		perm = new KatelloPermission(null, _perm[1], _permorg[1], "activation_keys", null, "manage_all", _permrole[1]);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permission create)");

		_permorg[2] = "perm-org2-"+ uid;
		org = new KatelloOrg(null,_permorg[2], null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		_permrole[2] = "perm-user_role2-"+ uid;
		user_role = new KatelloUserRole(null, _permrole[2], "User Roles to add Permissions");
		res = user_role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user role create)");
		_init_role_count++;
		
		_perm[2] = "perm2-"+uid;
		perm = new KatelloPermission(null, _perm[2], _permorg[2], "roles", null,"create, delete, update, read", _permrole[2]);
		res = perm.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permission create)");
		res = perm.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permission delete)");			
	}

	@Test(description="verify multiply permissions survived the upgrade - delete some of them", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkMultPermissionsSurvived(){

		KatelloPermission perm = new KatelloPermission(null, _perm[0], _permorg[0],"users", null, "create, delete, update, read",_permrole[0]);
		SSHCommandResult res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm[0]), "permission must be listed");

		//Delete some of the permissions after upgrade
		perm = new KatelloPermission(null, _perm[1], _permorg[1], "activation_keys", null, "manage_all", _permrole[1]);
		res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm[1]), "permission must be listed");
		res = perm.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (permission delete)");		

		// Check the non-existence of deleted permissions
		perm = new KatelloPermission(null, _perm[2], _permorg[2], "roles", null,"create, delete, update, read", _permrole[2]);
		res = perm.list();
		Assert.assertFalse(res.getStdout().contains(_perm[2]), "permission not listed");	
	}

	@Test(description="Create a Org Add default info - Check the existence after upgrade", 
			dependsOnMethods={"init"},
			groups={TNG_PRE_UPGRADE})
	public void addDefaultOrgInfo(){
		String uid = KatelloUtils.getUniqueID();
		_org_default_info[0] = "org_default_info0-"+uid;
		_keyname[0] = "test_key_before_upgrade0_"+ uid;
		KatelloOrg org = new KatelloOrg(null,_org_default_info[0], null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_add_old(_keyname[0]);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_apply_old();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		//Remove info before upgrade 
		_org_default_info[1] = "org_default_info1-"+uid;
		_keyname[1] = "test_key_before_upgrade1_"+ uid;
		org = new KatelloOrg(null,_org_default_info[1], null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_add_old(_keyname[1]);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_apply_old();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_remove_old(_keyname[1]);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
	}

	@Test(description="verify default info for org survived,Add new default info,Add distributor info", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkDefaultOrgInfoSurvived(){
		String uid = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg(null,_org_default_info[0], null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_keyname[0]), "Default Info keyname is in output");

		//Check the non-existence of deleted default info for org
		org = new KatelloOrg(null,_org_default_info[1], null);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertFalse(res.getStdout().contains(_keyname[1]), "Default Info keyname is not in output");

		//Add new default info to Org after upgrade
		_keyname[1] = "keyname_after_upgrade0_" + uid;
		res = org.default_info_add(_keyname[1], "system");
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_apply("system");
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_keyname[1]), "Default Info keyname is in output");	

		//Delete default info for Org after upgrade
		_keyname[1] = "keyname_after_upgrade1_" + uid;
		res = org.default_info_add(_keyname[1], "system");
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_apply("system");
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_keyname[1]), "Default Info keyname is in output");	
		res = org.default_info_remove(_keyname[1], "system");
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertFalse(res.getStdout().contains(_keyname[1]), "Default Info keyname is not in output");

		//Add default custom info for distributor at Org level
		_keyname[1] = "dist_key_after_upgrade0" + uid;
		res = org.default_info_add(_keyname[1], "distributor");
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = org.default_info_apply("distributor");
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		String _distname = "dist_name_"+uid;
		KatelloDistributor dist = new KatelloDistributor(null,_org_default_info[1],_distname);
		res = dist.distributor_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = dist.distributor_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_keyname[1]), "Default Info keyname is in output");	
	}

	@Test(description="Add user default Org,Assign user role - Perform operations before upgrade", 
			dependsOnMethods={"init"},
			groups={TNG_PRE_UPGRADE})
	public void addUserDefaultOrg(){
		String uid = KatelloUtils.getUniqueID();
		_org_default = "org-default-"+ uid;
		orgDefault = new KatelloOrg(null,_org_default, null);
		SSHCommandResult res = orgDefault.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		userOrgDefault = new KatelloUser(null, _user_default_org, 
				KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false , _org_default , KatelloEnvironment.LIBRARY);
		res = userOrgDefault.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		res = userOrgDefault.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		Assert.assertTrue(res.getStdout().contains(_org_default), "User info has default Org Assigned");

		_role_default = "user_role_" + uid;
		userRoleAssign = new KatelloUserRole(null,_role_default ,null);
		res = userRoleAssign.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		

		// Assign a user role to the user
		res = userOrgDefault.assign_role(_role_default);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		res = userOrgDefault.list_roles();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		Assert.assertTrue(res.getStdout().contains(_role_default), "User role found in the list");
	}

	@Test(description="verify user with default Org and User role assigned- Perform operations after upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkUserDefaultOrgSurvived(){
		// Check the user details after upgrade
		SSHCommandResult res = userOrgDefault.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		res = userOrgDefault.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		Assert.assertTrue(res.getStdout().contains(_org_default), "User info has default Org Assigned after upgrade");

		// Delete the assigned default org
		res = orgDefault.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		//Check the user info - details
		res = userOrgDefault.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		Assert.assertFalse(res.getStdout().contains(_org_default), "User info has default Org unAssigned");

		//Delete the user role and Check that it is unassigned to user after upgrade
		res = userRoleAssign.cli_delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		res = userOrgDefault.list_roles();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");		
		Assert.assertFalse(res.getStdout().contains(_role_default), "User role not found in the list");
	}

	@Test(description="Activation keys - Perform operations before upgrade", 
			groups={TNG_PRE_UPGRADE})
	public void activationKeyOperation(){
		String uid = KatelloUtils.getUniqueID();
		_act_key[0] = "act_key_" + uid;
		_org_act_key[0] = "org_act_key_" + uid;
		_env_act_key[0] = "env_act_key_" + uid;
		_sys_act_key[0] = "sys_Act_key_" + uid;

		KatelloOrg org = new KatelloOrg(null,_org_act_key[0], null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		KatelloEnvironment env = new KatelloEnvironment(null, _env_act_key[0], null, _org_act_key[0], KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org_act_key[0], _env_act_key[0], _act_key[0], null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		KatelloUtils.scpOnClient(null, "data/manifest_org_sla.zip", "/tmp");

		KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _org_act_key[0], null, null);
		res = rh.import_manifest("/tmp/manifest_org_sla.zip", null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		org = new KatelloOrg(null, _org_act_key[0], null);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		// getting poolid could vary - might be need to make switch case here for different versions...
		_poolRhel2 = KatelloUtils.grepCLIOutput("ID", KatelloCliTestBase.sgetOutput(res));
		if (_poolRhel2 == null || _poolRhel2.isEmpty()) {
			_poolRhel2 = KatelloUtils.grepCLIOutput("Id", KatelloCliTestBase.sgetOutput(res));
		}

		KatelloSystem sys = new KatelloSystem(null, _sys_act_key[0], _org_act_key[0], _env_act_key[0]);
		sys.runOn(SetupServers.client_name);
		KatelloUtils.sshOnClient(SetupServers.client_name, "subscription-manager clean");
		res = sys.rhsm_registerForce(_act_key[0]);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register");
		res = sys.rhsm_subscribe(_poolRhel2);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = sys.rhsm_identity();
		_sys_act_key_ID[0] = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		sys.runOn(null);
		res = sys.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_sys_act_key[0]), "Check the system is listed");

		// Setup the system which can be removed after upgrade
		uid = KatelloUtils.getUniqueID();
		_act_key[1] = "act_key_" + uid;
		_org_act_key[1] = "org_act_key_" + uid;
		_env_act_key[1] = "env_act_key_" + uid;
		_sys_act_key[1] = "sys_Act_key_" + uid;

		org = new KatelloOrg(null,_org_act_key[1], null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		env = new KatelloEnvironment(null, _env_act_key[1], null, _org_act_key[1], KatelloEnvironment.LIBRARY);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		key = new KatelloActivationKey(null, 
				_org_act_key[1], _env_act_key[1], _act_key[1], null, null);
		res = key.create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		KatelloUtils.scpOnClient(null, "data/manifest-sam-1.2.zip", "/tmp");

		rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _org_act_key[1], null, null);
		res = rh.import_manifest("/tmp/manifest-sam-1.2.zip", null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		org = new KatelloOrg(null, _org_act_key[1], null);
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		// getting poolid could vary - might be need to make switch case here for different versions...
		_poolRhel3 = KatelloUtils.grepCLIOutput("ID", KatelloCliTestBase.sgetOutput(res));
		if (_poolRhel3 == null || _poolRhel3.isEmpty()) {
			_poolRhel3 = KatelloUtils.grepCLIOutput("Id", KatelloCliTestBase.sgetOutput(res));
		}

		sys = new KatelloSystem(null, _sys_act_key[1], _org_act_key[1], _env_act_key[1]);
		sys.runOn(SetupServers.client_name2);
		KatelloUtils.sshOnClient(SetupServers.client_name2, "subscription-manager clean");
		res = sys.rhsm_registerForce(_act_key[1]);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register");
		res = sys.rhsm_subscribe(_poolRhel3);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		res = sys.rhsm_identity();
		_sys_act_key_ID[1] = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		sys.runOn(null);
		res = sys.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_sys_act_key[1]), "Check the system is listed");
	}

	@Test(description="verify the existence of scenarios performed on activation keys", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkActKeyOperationsSurvived(){
		// check that system survived the upgrade
		KatelloSystem sys = new KatelloSystem(null, _sys_act_key[0], _org_act_key[0], null);
		SSHCommandResult res = sys.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_sys_act_key[0]), "Check the system is listed");
		
		//Delete the activation keys and check the system is still registered after upgrade

		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org_act_key[0], null, _act_key[0], null, null);
		res = key.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");

		res = sys.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_sys_act_key[0]), "Check the system is still registered");

		//Remove the system after upgrade
		sys = new KatelloSystem(null, _sys_act_key[1], _org_act_key[1], null);
		sys.setUuid(_sys_act_key_ID[1]);
		res = sys.unregister();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertFalse(res.getStdout().contains(_sys_act_key[1]), "System removed succesfully");
	}

	@Test(description="Import Manifest in Org - Delete the manifest,and the org", 
			groups={TNG_PRE_UPGRADE})
	public void deleteOrgManifest(){
		KatelloUser user = new KatelloUser(null, _admin_user2,	KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		
		//create org, import manifest, register a client
		String uid = KatelloUtils.getUniqueID(); 
		_del_org[0] = "del-org0-"+ uid;
		KatelloOrg org = new KatelloOrg(null,_del_org[0], null);
		org.runAs(user);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, "/tmp");

		KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _del_org[0], null, null);
		rh.runAs(user);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		
		// getting poolid could vary - might be need to make switch case here for different versions...
		String pool = KatelloUtils.grepCLIOutput("ID", KatelloCliTestBase.sgetOutput(res));
		if (pool == null || pool.isEmpty()) {
			pool = KatelloUtils.grepCLIOutput("Id", KatelloCliTestBase.sgetOutput(res));
		}
		String system = "delsystem" + uid;
		String env_name = "env" + uid;
		KatelloEnvironment env = new KatelloEnvironment(null, env_name, null, _del_org[0], KatelloEnvironment.LIBRARY);
		env.runAs(user);
		res = env.cli_create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - env create");
		
		KatelloSystem sys = new KatelloSystem(null, system, _del_org[0], env_name);
		sys.runOn(SetupServers.client_name3);
		KatelloUtils.sshOnClient(SetupServers.client_name3, "subscription-manager clean");
		res = sys.rhsm_registerForce();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register");
		res = sys.rhsm_subscribe(pool);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm subscribe");
		
		
		// create org, import manifest, delete org
		_del_org[1] = "del-org1-"+ uid;
		org = new KatelloOrg(null,_del_org[1], null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_SAM_MATRIX, "/tmp");

		rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _del_org[1], null, null);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_SAM_MATRIX, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		res = rh.delete_manifest();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		
		_del_org[2] = "del-org2-"+ uid;
		org = new KatelloOrg(null,_del_org[2], null);
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
	}

	@Test(description="verify the existence of manifest org, re-create deleted org and re-import manifest", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgsManifestSurvived(){
		KatelloUser user = new KatelloUser(null, _admin_user2,	KatelloUser.DEFAULT_USER_EMAIL, System.getProperty("katello.admin.password", KatelloUser.DEFAULT_ADMIN_PASS), false);
		
		// check org with manifest is survived, delete org
		KatelloOrg org = new KatelloOrg(null, _del_org[0], null);
		org.runAs(user);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org delete");		
		
		// re-import manifest in other org, delete the org
		KatelloUtils.scpOnClient(null, "data/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, "/tmp");
		org = new KatelloOrg(null,_del_org[2], null);
		org.runAs(user);
		KatelloProvider rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _del_org[2], null, null);
		rh.runAs(user);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_2SUBSCRIPTIONS, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		res = org.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org delete");
		
		// Check the non-existence of deleted Org,manifest
		org = new KatelloOrg(null, _del_org[1], null);
		org.runAs(user);
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==148, "Check - exit code (org info)");

		//Reimport the deleted manifest to check it works
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _del_org[1], null, null);
		rh.runAs(user);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_SAM_MATRIX, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		
		//again delete the org and re-create it with re-importing manifest
		res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");	
		
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		rh = new KatelloProvider(null, KatelloProvider.PROVIDER_REDHAT, _del_org[1], null, null);
		rh.runAs(user);
		res = rh.import_manifest("/tmp/"+KatelloProvider.MANIFEST_SAM_MATRIX, null);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - provider import_manifest");
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - org subscriptions");
		
		res = user.delete();
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - user delete");
	}

	@Test(description="verify orgs survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgsSurvived(){
		KatelloOrg org = new KatelloOrg(null, _org, null);
		SSHCommandResult res = org.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org info)");
	}
	
	@Test(description="verify user survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkUserSurvived(){
		KatelloUser user = new KatelloUser(null, _user, 
				null, null, false);
		SSHCommandResult res = user.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user info)");
	}
	
	@Test(description="verify environments survived the upgrade as a created system groups", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkEnvironmentsSurvived() {
		KatelloEnvironment env = new KatelloEnvironment(null, null, null, _org, KatelloEnvironment.LIBRARY);
		SSHCommandResult res = env.cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertFalse(res.getStdout().contains(_env_1), "Environment name is not in environment list output");
		Assert.assertFalse(res.getStdout().contains(_env_2), "Environment name is not in environment list output");
		Assert.assertFalse(res.getStdout().contains(_env_3), "Environment name is not in environment list output");
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(null, _env_1, _org);
		res = systemGroup.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group info)");
		Assert.assertTrue(res.getStdout().contains(_env_1), "Environment name is in system group info output");
		
		systemGroup = new KatelloSystemGroup(null, _env_2, _org);
		res = systemGroup.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group info)");
		Assert.assertTrue(res.getStdout().contains(_env_2), "Environment name is in system group info output");
		
		systemGroup = new KatelloSystemGroup(null, _env_3, _org);
		res = systemGroup.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group info)");
		Assert.assertTrue(res.getStdout().contains(_env_3), "Environment name is in system group info output");
	}
	
	@Test(description="verify role survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE}, dependsOnMethods={"checkMultUserRoleSurvived"})
	public void checkRoleSurvived(){
		KatelloUserRole user_role = new KatelloUserRole(null, _user_role, null);
		SSHCommandResult res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_user_role), "Role name is in info output");
		
		user_role = new KatelloUserRole(null, _user_role2, null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_user_role2), "Role name is in info output");
		
		user_role = new KatelloUserRole(null, _user_role3, null);
		res = user_role.cli_info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role info)");
		Assert.assertTrue(res.getStdout().contains(_user_role3), "Role name is in info output");
		
		res = user_role.cli_list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (role list)");
		Assert.assertTrue(res.getStdout().contains(_user_role), "Role name is in list output");
		Assert.assertTrue(res.getStdout().contains(_user_role2), "Role name is in list output");
		Assert.assertTrue(res.getStdout().contains(_user_role3), "Role name is in list output");
		
		int count = Integer.parseInt(user_role.cli_list_count().getStdout().trim());
		
		Assert.assertEquals(count, _init_role_count, "Count of roles should remain the same after upgrade");
	}
	
	@Test(description="verify activation key survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkActivationKeySurvived(){
		KatelloActivationKey key = new KatelloActivationKey(null, 
				_org, null, _akey, null, null);
		SSHCommandResult res = key.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		
		key = new KatelloActivationKey(null, _org, null, _akey2, null, null);
		res = key.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		
		key = new KatelloActivationKey(null, _org, null, _akey3, null, null);
		res = key.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
	}
	
	@Test(description="verify systems survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkSystemsSurvived(){
		KatelloSystem sys = new KatelloSystem(null, _system, _org, null);
		SSHCommandResult res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		Assert.assertTrue(res.getStdout().contains(_system), "System name is in info output");
		Assert.assertTrue(res.getStdout().contains(KatelloEnvironment.LIBRARY), "Library is in info output");
		Assert.assertTrue(res.getStdout().contains(_akey), "Activation key is in info output");
		
		sys = new KatelloSystem(null, _system2, _org, null);
		res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in info output");
		Assert.assertTrue(res.getStdout().contains(KatelloEnvironment.LIBRARY), "Library is in info output");
		Assert.assertTrue(res.getStdout().contains(_akey2), "Activation key is in info output");
		
		sys = new KatelloSystem(null, _system2, _org, null);
		res = sys.info();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (activation key info)");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in info output");
		Assert.assertTrue(res.getStdout().contains(KatelloEnvironment.LIBRARY), "Library is in info output");
		
		res = sys.list();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (system list)");
		Assert.assertTrue(res.getStdout().contains(_system), "System name is in list output");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in list output");
		Assert.assertTrue(res.getStdout().contains(_system3), "System name is in list output");
		
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(null, _env_1, _org);
		res = systemGroup.list_systems();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_system), "System name is in list output");
		Assert.assertFalse(res.getStdout().contains(_system2), "System name is not in list output");
		Assert.assertFalse(res.getStdout().contains(_system3), "System name is not in list output");
		
		systemGroup = new KatelloSystemGroup(null, _env_2, _org);
		res = systemGroup.list_systems();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_system2), "System name is in list output");
		Assert.assertFalse(res.getStdout().contains(_system), "System name is not in list output");
		Assert.assertFalse(res.getStdout().contains(_system3), "System name is not in list output");
		
		systemGroup = new KatelloSystemGroup(null, _env_3, _org);
		res = systemGroup.list_systems();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code");
		Assert.assertTrue(res.getStdout().contains(_system3), "System name is in list output");
		Assert.assertFalse(res.getStdout().contains(_system2), "System name is not in list output");
		Assert.assertFalse(res.getStdout().contains(_system), "System name is not in list output");
	}
	
	@Test(description="verify that permissions not related to environments survived the upgrade", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkPermissionSurvived(){
		KatelloPermission perm = new KatelloPermission(null, null, null, null, null, null, _user_role);
		SSHCommandResult res = perm.list();
		// permissions for environments must be gone
		Assert.assertFalse(res.getStdout().contains(_perm_1), "permission must be listed");
		
		perm = new KatelloPermission(null, null, null, null, null, null, _user_role2);
		res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm_2), "permission must be listed");
		
		perm = new KatelloPermission(null, null, null, null, null, null, _user_role3);
		res = perm.list();
		Assert.assertTrue(res.getStdout().contains(_perm_3), "permission must be listed");
	}
	
	@Test(description="verify it is possible to create org", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkOrgCreate(){
		KatelloOrg org = new KatelloOrg(null, _neworg, null);
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org create)");		
	}

	@Test(description="verify it is possible to remove org", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE}, dependsOnMethods={"checkOrgCreate"})
	public void checkOrgRemove(){
		KatelloOrg org = new KatelloOrg(null, _neworg, null);
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (org remove)");		
	}
	
	@Test(description="verify it is possible to create user", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkUserCreate(){
		KatelloUser user = new KatelloUser(null, _newuser, "newuser@localhost", "redhat", false);
		SSHCommandResult res = user.cli_create();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user create)");	
	}

	@Test(description="verify it is possible to remove user", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE}, dependsOnMethods={"checkUserCreate"})
	public void checkUserRemove(){
		KatelloUser user = new KatelloUser(null, _newuser, "newuser@localhost", "redhat", false);
		SSHCommandResult res = user.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (user remove)");		
	}
	
	@Test(description="verify it is possible to create system group, and add systems into it and delete system and group", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE}, dependsOnMethods={"checkSystemsSurvived"})
	public void checkSysGroupCreate(){
		KatelloSystemGroup systemGroup = new KatelloSystemGroup(null, _system_group, _org);
		SSHCommandResult res = systemGroup.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group create)");

    	KatelloSystem sys = new KatelloSystem(null, _system, _org, null);
    	sys.runOn(SetupServers.client_name);
    	res = sys.rhsm_identity();
		String system_uuid = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		
		res = systemGroup.add_systems(system_uuid);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
		
		sys = new KatelloSystem(null, _newsystem, _org, null);
		sys.runOn(SetupServers.client_name2);
		KatelloUtils.sshOnClient(SetupServers.client_name2, "subscription-manager clean");
		res = sys.rhsm_registerForce(_akey);
		Assert.assertTrue(res.getExitCode().intValue()==0, "exit(0) - rhsm register (activationkey)");
		
		res = sys.rhsm_identity();
		String system_uuid2 = KatelloUtils.grepCLIOutput("Current identity is", res.getStdout());
		
		res = systemGroup.add_systems(system_uuid2);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
		
		res = systemGroup.remove_systems(system_uuid2);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (reomve system)");
		
		KatelloSystemGroup systemGroup2 = new KatelloSystemGroup(null, _system_group2, _org);
		res = systemGroup2.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (system group create)");
		
		res = systemGroup2.add_systems(system_uuid);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
		
		res = systemGroup2.add_systems(system_uuid2);
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (add system)");
		
		res = systemGroup.delete();
		Assert.assertTrue(res.getExitCode()==0, "Check - exit code (delete system group)");
	}
	
	@Test(description="Add multiple SAM Distributors. Delete some of them", 
			dependsOnGroups={TNG_PRE_UPGRADE, TNG_UPGRADE}, 
			groups={TNG_POST_UPGRADE})
	public void checkDistroCreate() {
		KatelloOrg org = new KatelloOrg(null, _org, null);
		
		SSHCommandResult res = org.default_info_add(_keyname1, "distributor");
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = org.default_info_apply("distributor");
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		KatelloDistributor distributor = new KatelloDistributor(null, _org, _distributor_name);
		res = distributor.distributor_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (create distributor)");

		res = distributor.add_info(_keyname2, "testvalue", null);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		res = distributor.subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (subscribe)");
		
		res = distributor.remove_info(_keyname1);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (subscribe)");
		
		res = distributor.remove_info(_keyname2);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (subscribe)");
		
		KatelloDistributor distributor2 = new KatelloDistributor(null, _org, _distributor_name2);
		res = distributor2.distributor_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (create distributor)");
		
		res = distributor2.subscribe(_poolRhel);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (subscribe)");
		
		res = distributor2.add_info(_keyname2, "testvalue", null);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		res = distributor2.distributor_delete();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (delete distributor)");
	}
	
	@AfterClass(description="Deleted created orgs from upgrade scenarios which contains manifests",
			alwaysRun=true)
	public void tearDown() {
		SSHCommandResult res = new KatelloOrg(null, _org, null).delete();
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code of main org delete");
		new KatelloOrg(null, _del_org[0], null).delete();
		new KatelloOrg(null, _del_org[1], null).delete();
		new KatelloOrg(null, _del_org[2], null).delete();
	}
}

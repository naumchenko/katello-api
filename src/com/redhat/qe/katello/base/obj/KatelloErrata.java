package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;

import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloErrata extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "errata info";
	public static final String CMD_LIST = "errata list";
	public static final String CMD_LIST_DETAILS = "errata list -v";
	
	public static final String REG_CHS_PROMOTE_ERROR = "Validation failed: Repository of the erratum '%s' has not been promoted into the target environment!";
	public static final String REG_CHS_DEL_ERROR = "Erratum not found within this environment you want to promote from.";
	
	// ** ** ** ** ** ** ** Class members
	String id;
	String org;
	String product;
	String repo;
	String environment;
	String product_id;
	String type;
	public String content_view;
	public String content_view_label;
	public String content_view_id;
	
	public KatelloErrata(String pId, String pOrg, String pProd, String pRepo, String pEnv){
		this(pId, pOrg, pProd, pRepo, pEnv, null);
	}

	public KatelloErrata(String pId, String pOrg, String pProd, String pRepo, String pEnv, String pType){
		this.id = pId;
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.environment = pEnv;
		this.type = pType;
	}
	
	public KatelloErrata(String pOrg, String pProd, String pRepo, String pContnetView) {
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.content_view = pContnetView;
	}
	
	public void setId(String pId) {
		this.id = pId;
	}
	
	public void setProductId(String productId) {
		this.product_id = productId;
		this.product = null;
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("org", org));
		if (this.product_id != null) {
			opts.add(new Attribute("product_id", product_id));
		} else {
			opts.add(new Attribute("product", product));
		}
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		return run(CMD_INFO);
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.product_id != null) {
			opts.add(new Attribute("product_id", product_id));
		} else {
			opts.add(new Attribute("product", product));
		}
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("type", type));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		return run(CMD_LIST);
	}

	public SSHCommandResult list_errata_details(){
		opts.clear();
		opts.add(new Attribute("org", org));
		if (this.product_id != null) {
			opts.add(new Attribute("product_id", product_id));
		} else {
			opts.add(new Attribute("product", product));
		}
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		opts.add(new Attribute("type", type));
		opts.add(new Attribute("content_view", content_view));
		opts.add(new Attribute("content_view_label", content_view_label));
		opts.add(new Attribute("content_view_id", content_view_id));
		return run(CMD_LIST_DETAILS);
	}
	
	public SSHCommandResult list_errata_count(String query) {
		String cmd = CMD_LIST;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if (this.product_id != null) {
			cmd += " --product_id \""+this.product_id+"\"";
		} else {
			cmd += " --product \""+this.product+"\"";
		}
		if(this.repo != null)
		cmd += " --repo \""+this.repo+"\"";
		if (this.content_view_id != null) {
			cmd += " --content_view_id \""+this.content_view_id+"\"";
		} else if (this.content_view_label != null) {
			cmd += " --content_view_label \""+this.content_view_label+"\"";
		} else {
			cmd += " --content_view \""+this.content_view+"\"";
		}
		
		if(this.environment != null)
		cmd += " --environment \""+this.environment+"\"";


		cmd += " | grep \"" + query + "\" | wc -l";
		
		KatelloCli cli = new KatelloCli(cmd, null);
		return cli.run();	
	}

	public SSHCommandResult list_errata_names(String query) {
		String cmd = CMD_LIST;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if (this.product_id != null) {
			cmd += " --product_id \""+this.product_id+"\"";
		} else {
			cmd += " --product \""+this.product+"\"";
		}
		if(this.repo != null)
		cmd += " --repo \""+this.repo+"\"";
		if (this.content_view_id != null) {
			cmd += " --content_view_id \""+this.content_view_id+"\"";
		} else if (this.content_view_label != null) {
			cmd += " --content_view_label \""+this.content_view_label+"\"";
		} else {
			cmd += " --content_view \""+this.content_view+"\"";
		}
		
		if(this.environment != null)
		cmd += " --environment \""+this.environment+"\"";

		cmd += " | grep \"" + query + "\" | awk '{print $1}'";
		
		KatelloCli cli = new KatelloCli(cmd, null);
		return cli.run();	
	}
	
	public SSHCommandResult list_errata_details_count(String query) {
		String cmd = CMD_LIST_DETAILS;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if (this.product_id != null) {
			cmd += " --product_id \""+this.product_id+"\"";
		} else {
			cmd += " --product \""+this.product+"\"";
		}
		if(this.repo != null)
		cmd += " --repo \""+this.repo+"\"";
		if (this.content_view_id != null) {
			cmd += " --content_view_id \""+this.content_view_id+"\"";
		} else if (this.content_view_label != null) {
			cmd += " --content_view_label \""+this.content_view_label+"\"";
		} else {
			cmd += " --content_view \""+this.content_view+"\"";
		}
		
		if(this.environment != null)
		cmd += " --environment \""+this.environment+"\"";

		cmd += " | grep \"" + query + "\" | wc -l";
		
		KatelloCli cli = new KatelloCli(cmd, null);
		return cli.run();	
	}

	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}

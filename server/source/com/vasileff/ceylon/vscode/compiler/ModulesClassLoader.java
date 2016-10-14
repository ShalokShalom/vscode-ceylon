package com.vasileff.ceylon.vscode.compiler;

import java.util.LinkedList;
import java.util.List;

import com.redhat.ceylon.model.cmr.ArtifactResult;
import com.redhat.ceylon.model.loader.JdkProvider;
import com.redhat.ceylon.model.loader.impl.reflect.CachedTOCJars;
import com.redhat.ceylon.model.typechecker.model.Module;

/**
 * Class loader which looks into a list of jar files
 */
class ModulesClassLoader extends ClassLoader {

    private CachedTOCJars jars = new CachedTOCJars();
	private JdkProvider jdkProvider;

    public ModulesClassLoader(ClassLoader parent, JdkProvider jdkProvider) {
        super(parent);
        this.jdkProvider = jdkProvider;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        byte[] contents = jars.getContents(path);
        if(contents != null)
            return defineClass(name, contents, 0, contents.length);
        return super.findClass(name);
    }

    public void addJar(ArtifactResult artifact, Module module, boolean skipContents) {
        jars.addJar(artifact, module, skipContents);
    }

    public boolean packageExists(Module module, String name) {
        String moduleName = module.getNameAsString();
        if(jdkProvider.isJDKModule(moduleName)){
            return jdkProvider.isJDKPackage(moduleName, name);
        }
        return jars.packageExists(module, name);
    }

    public List<String> getPackageList(Module module, String name) {
        String moduleName = module.getNameAsString();
        if(jdkProvider.isJDKModule(moduleName)){
            return getJDKPackageList(name);
        }
        return jars.getPackageList(module, name);
    }

    private List<String> getJDKPackageList(String name) {
    	List<String> ret = new LinkedList<String>();
    	String prefix = name+".";
    	for(String pkg : jdkProvider.getJDKPackageList()){
    		if(pkg.equals(name) || pkg.startsWith(prefix))
    			ret.add(pkg);
    	}
    	return ret;
    }
}
/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.properties.assistant.lastaflute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.logic.manage.freegen.DfFreeGenResourceType;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author p1us2er0
 * @since 1.1.0-sp3 (2015/04/26 Sunday)
 */
public final class DfLastaFluteFreeGenReflector {

    private static final Logger logger = LoggerFactory.getLogger(DfLastaFluteFreeGenReflector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _freeGenMap;
    protected final String _capServiceName;
    protected final String _uncapServiceName;
    protected final String _domainPackage;
    protected final String _appPackage;
    protected final String _mylastaPackage;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaFluteFreeGenReflector(Map<String, Object> freeGenMap, String serviceName, String domainPackage) {
        _freeGenMap = freeGenMap;
        _capServiceName = Srl.initCap(serviceName);
        _uncapServiceName = Srl.initUncap(serviceName);
        _domainPackage = domainPackage;
        _appPackage = domainPackage + ".app";
        _mylastaPackage = domainPackage + ".mylasta";
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void reflectFrom(Map<String, Object> lastafluteMap, String lastaDocOutputDirectory) {
        logger.info("Before LastaFlute refecting, existing freeGen settigs: " + _freeGenMap.keySet());
        boolean hasCommonEnv = false;
        boolean hasCommonConfig = false;
        boolean hasCommonLabel = false;
        boolean hasCommonMessage = false;
        @SuppressWarnings("unchecked")
        final Map<String, Object> commonMap = (Map<String, Object>) lastafluteMap.get("commonMap");
        if (commonMap != null) {
            final String path = (String) commonMap.get("path");
            @SuppressWarnings("unchecked")
            final List<String> freeGenList = (List<String>) commonMap.get("freeGenList");
            for (String freeGen : freeGenList) {
                logger.info("...Reflecting common freeGen settigs: " + freeGen + ", " + path);
                if ("env".equals(freeGen)) {
                    setupEnvGen(_uncapServiceName, path, false, false);
                    hasCommonEnv = true;
                } else if ("config".equals(freeGen)) {
                    final String pluginInterface = (String) commonMap.get("configPluginInterface");
                    setupConfigGen(_uncapServiceName, path, hasCommonEnv, false, false, pluginInterface);
                    hasCommonConfig = true;
                } else if ("label".equals(freeGen)) {
                    setupLabelGen(_uncapServiceName, path, false, false, lastafluteMap);
                    hasCommonLabel = true;
                } else if ("message".equals(freeGen)) {
                    setupMessageGen(_uncapServiceName, path, hasCommonLabel, false, false, lastafluteMap);
                    hasCommonMessage = true;
                } else if ("mail".equals(freeGen)) {
                    final String pluginInterface = (String) commonMap.get("mailPluginInterface");
                    setupMailFluteGen(_uncapServiceName, path, lastafluteMap, pluginInterface);
                } else if ("template".equals(freeGen)) {
                    setupPmTemplateGen(_uncapServiceName, path, lastafluteMap);
                } else if ("doc".equals(freeGen)) {
                    setupDocGen(_uncapServiceName, path, lastaDocOutputDirectory);
                } else {
                    String msg = "Unkonwn type for commonMap's freeGen: " + freeGen;
                    throw new DfIllegalPropertySettingException(msg);
                }
            }
        }
        boolean hasAppEnv = false;
        boolean hasAppLabel = false;
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> appMap = (Map<String, Map<String, Object>>) lastafluteMap.get("appMap");
        if (appMap != null) {
            for (Entry<String, Map<String, Object>> entry : appMap.entrySet()) {
                final String appName = entry.getKey();
                final Map<String, Object> defMap = entry.getValue();
                final String path = (String) defMap.get("path");
                @SuppressWarnings("unchecked")
                final List<String> freeGenList = (List<String>) defMap.get("freeGenList");
                for (String freeGen : freeGenList) {
                    logger.info("...Reflecting application freeGen settigs: " + appName + "." + freeGen);
                    if ("env".equals(freeGen)) {
                        setupEnvGen(appName, path, hasCommonEnv, hasCommonConfig);
                        hasAppEnv = true;
                    } else if ("config".equals(freeGen)) {
                        final String pluginInterface = (String) defMap.get("configPluginInterface");
                        setupConfigGen(appName, path, hasCommonEnv, hasCommonConfig, hasAppEnv, pluginInterface);
                    } else if ("label".equals(freeGen)) {
                        setupLabelGen(appName, path, hasCommonLabel, hasCommonMessage, lastafluteMap);
                        hasAppLabel = true;
                    } else if ("message".equals(freeGen)) {
                        setupMessageGen(appName, path, hasCommonLabel, hasCommonMessage, hasAppLabel, lastafluteMap);
                    } else if ("mail".equals(freeGen)) {
                        final String pluginInterface = (String) defMap.get("mailPluginInterface");
                        setupMailFluteGen(appName, path, lastafluteMap, pluginInterface);
                    } else if ("template".equals(freeGen)) {
                        setupPmTemplateGen(appName, path, lastafluteMap);
                    } else if ("jsp".equals(freeGen)) {
                        setupJspPathGen(appName, path, lastafluteMap);
                    } else if ("html".equals(freeGen)) {
                        setupHtmlPathGen(appName, path, lastafluteMap);
                    } else if ("doc".equals(freeGen)) {
                        setupDocGen(appName, path, lastaDocOutputDirectory);
                    } else {
                        String msg = "Unkonwn type for appMap's freeGen: " + freeGen;
                        throw new DfIllegalPropertySettingException(msg);
                    }
                }
            }
        }
        showFreeGenSettings();
    }

    protected void showFreeGenSettings() {
        final StringBuilder sb = new StringBuilder();
        sb.append("After LastaFlute refecting, existing freeGen settigs: ").append(_freeGenMap.keySet());
        for (Entry<String, Object> entry : _freeGenMap.entrySet()) {
            sb.append("\n ").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        logger.info(sb.toString());
    }

    // ===================================================================================
    //                                                                       Configuration
    //                                                                       =============
    protected void setupEnvGen(String appName, String path, boolean hasCommonEnv, boolean hasCommonConfig) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "env";
        registerFreeGen(initCap(appName) + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupOutputConfigMap(appName, elementMap, theme);
        final Map<String, Object> tableMap = createTableMap();
        elementMap.put("tableMap", tableMap);
        if (!hasCommonEnv && !hasCommonConfig) { // root
            doSetupConfigTableMapRoot(tableMap);
        } else {
            final String parentName = _uncapServiceName;
            final String parentTheme = hasCommonConfig ? "config" : "env";
            doSetupConfigTableMapInheritance(tableMap, parentName, parentTheme);
        }
    }

    protected void setupConfigGen(String appName, String path, boolean hasCommonEnv, boolean hasCommonConfig, boolean hasAppEnv,
            String pluginInterface) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        final String theme = "config";
        registerFreeGen(capAppName + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupOutputConfigMap(appName, elementMap, theme);
        final Map<String, Object> tableMap = createTableMap();
        elementMap.put("tableMap", tableMap);
        if (pluginInterface != null) {
            tableMap.put("pluginInterface", pluginInterface);
        }
        if (!hasCommonEnv && !hasCommonConfig && !hasAppEnv) { // root
            doSetupConfigTableMapRoot(tableMap);
        } else {
            final String parentName = hasAppEnv ? appName : _uncapServiceName;
            final String parentTheme = hasAppEnv ? "env" : (hasCommonConfig ? "config" : "env");
            doSetupConfigTableMapInheritance(tableMap, parentName, parentTheme);
        }
    }

    protected void doSetupOutputConfigMap(String appName, Map<String, Map<String, Object>> elementMap, String theme) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        elementMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaSystemConfig.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", _mylastaPackage + ".direction");
        outputMap.put("className", initCap(appName) + initCap(theme));
    }

    protected void doSetupConfigTableMapRoot(Map<String, Object> tableMap) {
        tableMap.put("superClassPackage", "org.lastaflute.core.direction");
        tableMap.put("superClassSimpleName", "ObjectiveConfig");
    }

    protected void doSetupConfigTableMapInheritance(Map<String, Object> tableMap, String appName, String theme) {
        tableMap.put("extendsPropRequest", initCap(appName) + buildTitleSuffix(theme));
        tableMap.put("isCheckImplicitOverride", getTrueLiteral());
        tableMap.put("interfacePackage", _mylastaPackage + ".direction");
        tableMap.put("interfaceSimpleName", initCap(appName) + initCap(theme));
        tableMap.put("superClassPackage", _mylastaPackage + ".direction");
        tableMap.put("superClassSimpleName", initCap(appName) + initCap(theme) + ".SimpleImpl");
    }

    // ===================================================================================
    //                                                                            Messages
    //                                                                            ========
    protected void setupLabelGen(String appName, String path, boolean hasCommonLabel, boolean hasCommonMessage,
            Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "label";
        registerFreeGen(initCap(appName) + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupMessageOutputMap(appName, elementMap, theme, lastafluteMap);
        final Map<String, Object> tableMap = createTableMap();
        elementMap.put("tableMap", tableMap);
        tableMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap(theme, "prefix:labels."));
        if (hasCommonLabel || hasCommonMessage) {
            final String parentName = _uncapServiceName;
            final String parentTheme = hasCommonMessage ? "message" : "label";
            doSetupMessageTableMapInheritance(tableMap, parentName, parentTheme, lastafluteMap);
        }
    }

    protected void setupMessageGen(String appName, String path, boolean hasCommonLabel, boolean hasCommonMessage, boolean hasAppLabel,
            Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> elementMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "message";
        registerFreeGen(initCap(appName) + buildTitleSuffix(theme), elementMap);
        doSetupResourceMap(appName, path, elementMap, theme);
        doSetupMessageOutputMap(appName, elementMap, theme, lastafluteMap);
        final Map<String, Object> tableMap = createTableMap();
        elementMap.put("tableMap", tableMap);
        tableMap.put("groupingKeyMap", DfCollectionUtil.newLinkedHashMap("label", "prefix:labels."));
        if (hasCommonLabel || hasCommonMessage || hasAppLabel) {
            final String parentName = hasAppLabel ? appName : _uncapServiceName;
            final String parentTheme = hasAppLabel ? "label" : (hasCommonMessage ? "message" : "label");
            doSetupMessageTableMapInheritance(tableMap, parentName, parentTheme, lastafluteMap);
        }
    }

    protected void doSetupMessageOutputMap(String appName, Map<String, Map<String, Object>> elementMap, String theme,
            Map<String, Object> lastafluteMap) {
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        elementMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaUserMessages.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildMessagesPackage(appName, lastafluteMap));
        outputMap.put("className", initCap(appName) + initCap(theme) + "s");
    }

    protected void doSetupMessageTableMapInheritance(Map<String, Object> tableMap, String appName, String theme,
            Map<String, Object> lastafluteMap) {
        tableMap.put("extendsPropRequest", initCap(appName) + buildTitleSuffix(theme));
        tableMap.put("isCheckImplicitOverride", getTrueLiteral());
        tableMap.put("isUseNonNumberVariable", getTrueLiteral());
        tableMap.put("superClassPackage", buildMessagesPackage(appName, lastafluteMap));
        tableMap.put("superClassSimpleName", initCap(appName) + initCap(theme) + "s");
    }

    protected String buildMessagesPackage(String appName, Map<String, Object> lastafluteMap) {
        final String messagesPackage = _mylastaPackage + ".action";
        return filterOverridden(messagesPackage, lastafluteMap, appName, "message", "package");
    }

    // ===================================================================================
    //                                                                           MailFlute
    //                                                                           =========
    protected void setupMailFluteGen(String appName, String path, Map<String, Object> lastafluteMap, String pluginInterface) {
        doSetupMailFluteGen(appName, path, "$$baseDir$$/resources/mail", "dfmail", lastafluteMap, pluginInterface);
    }

    protected void doSetupMailFluteGen(String appName, String path, String targetDir, String ext, Map<String, Object> lastafluteMap,
            String pluginInterface) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + "MailFlute", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.MAIL_FLUTE.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaMailFlute.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildMailPostcardPackage(capAppName, lastafluteMap));
        outputMap.put("className", initCap(appName) + "Postcard");
        final Map<String, Object> tableMap = createTableMap();
        pathMap.put("tableMap", tableMap);
        tableMap.put("targetDir", filterOverridden(targetDir, lastafluteMap, appName, "mail", "targetDir"));
        tableMap.put("targetExt", filterOverridden("." + ext, lastafluteMap, appName, "mail", "targetExt"));
        final List<String> exceptPathList = DfCollectionUtil.newArrayList("contain:/mail/common/");
        tableMap.put("exceptPathList", filterOverridden(exceptPathList, lastafluteMap, appName, "mail", "exceptPathList"));
        if (pluginInterface != null) {
            tableMap.put("pluginInterface", pluginInterface);
        }
    }

    protected String buildMailPostcardPackage(String appName, Map<String, Object> lastafluteMap) {
        final String generatedPackage = _mylastaPackage + ".mail";
        return filterOverridden(generatedPackage, lastafluteMap, appName, "mail", "package");
    }

    // ===================================================================================
    //                                                                          PmTemplate
    //                                                                          ==========
    protected void setupPmTemplateGen(String appName, String path, Map<String, Object> lastafluteMap) {
        doSetupPmTemplateGen(appName, path, "$$baseDir$$/resources", "dfpm", lastafluteMap);
    }

    protected void doSetupPmTemplateGen(String appName, String path, String targetDir, String ext, Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + "PmTemplate", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.PM_FILE.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaPmTemplate.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildPmTemplateBeanPackage(capAppName, lastafluteMap));
        outputMap.put("className", "unused");
        final Map<String, Object> tableMap = createTableMap();
        pathMap.put("tableMap", tableMap);
        tableMap.put("targetDir", filterOverridden(targetDir, lastafluteMap, appName, "template", "targetDir"));
        tableMap.put("targetExt", filterOverridden("." + ext, lastafluteMap, appName, "template", "targetExt"));
        tableMap.put("isConventionSuffix", "true");
        tableMap.put("isLastaTemplate", "true");
    }

    protected String buildPmTemplateBeanPackage(String appName, Map<String, Object> lastafluteMap) {
        final String generatedPackage = _mylastaPackage; // template package is added later if convention
        return filterOverridden(generatedPackage, lastafluteMap, appName, "template", "package");
    }

    // ===================================================================================
    //                                                                       HTML Template
    //                                                                       =============
    protected void setupJspPathGen(String appName, String path, Map<String, Object> lastafluteMap) {
        final String targetDir = "$$baseDir$$/webapp/WEB-INF/view";
        doSetupHtmlTemplatePathGen(appName, path, targetDir, "jsp", lastafluteMap);
    }

    protected void setupHtmlPathGen(String appName, String path, Map<String, Object> lastafluteMap) {
        // daone jflute lastaflute: thinking Thymeleaf localtion => same as JSP for now
        //final String targetDir = "$$baseDir$$/resources/templates";
        final String targetDir = "$$baseDir$$/webapp/WEB-INF/view";
        doSetupHtmlTemplatePathGen(appName, path, targetDir, "html", lastafluteMap);
    }

    protected void doSetupHtmlTemplatePathGen(String appName, String path, String targetDir, String ext, Map<String, Object> lastafluteMap) {
        final Map<String, Map<String, Object>> pathMap = new LinkedHashMap<String, Map<String, Object>>();
        final String capAppName = initCap(appName);
        registerFreeGen(capAppName + initCap(ext) + "Path", pathMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        pathMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.FILE_PATH.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        pathMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaHtmlPath.vm");
        outputMap.put("outputDirectory", "$$baseDir$$/java");
        outputMap.put("package", buildHtmlTemplatePackage(capAppName, lastafluteMap, ext));
        outputMap.put("className", initCap(appName) + "HtmlPath");
        final Map<String, Object> tableMap = createTableMap();
        pathMap.put("tableMap", tableMap);
        tableMap.put("targetDir", targetDir);
        tableMap.put("targetExt", ext.equals("html") ? ".html" : ("." + ext + "|.html"));
        tableMap.put("exceptPathList", DfCollectionUtil.newArrayList("contain:/view/common/"));
    }

    protected String buildHtmlTemplatePackage(String appName, Map<String, Object> lastafluteMap, String ext) {
        // for simple rule "generate and reboot", making HTML file not always need hot-deploy
        //final String generatedPackage = _appPackage + ".web.base.html";
        final String generatedPackage = _mylastaPackage + ".action";
        return filterOverridden(generatedPackage, lastafluteMap, appName, ext, "package");
    }

    // ===================================================================================
    //                                                                            Document
    //                                                                            ========
    // last process recommended for generated classes by others
    protected void setupDocGen(String appName, String path, String lastaDocOutputDirectory) {
        final Map<String, Map<String, Object>> docMap = new LinkedHashMap<String, Map<String, Object>>();
        final String theme = "lastaDoc";
        registerFreeGen(initCap(appName) + buildTitleSuffix(theme), docMap);
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        docMap.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.LASTA_DOC.name());
        final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        docMap.put("outputMap", outputMap);
        outputMap.put("templateFile", "LaDocHtml.vm");
        outputMap.put("outputDirectory", lastaDocOutputDirectory);
        outputMap.put("package", ""); // flat documents e.g. ./output/doc/lastadoc-harbor.html
        outputMap.put("className", "lastadoc-" + appName);
        outputMap.put("fileExt", "html");
        final Map<String, Object> tableMap = createTableMap();
        docMap.put("tableMap", tableMap);
        tableMap.put("path", path);
        tableMap.put("targetDir", "$$baseDir$$/java");
        tableMap.put("appName", appName);
    }

    // ===================================================================================
    //                                                                           Core Info
    //                                                                           =========
    protected void registerFreeGen(String key, Map<String, Map<String, Object>> map) {
        final Object existing = _freeGenMap.get(key);
        if (existing != null) {
            String msg = "Found the existing same-name setting: key=" + key + " existing=" + existing;
            throw new DfIllegalPropertySettingException(msg);
        }
        _freeGenMap.put(key, map);
    }

    protected void doSetupResourceMap(String appName, String path, Map<String, Map<String, Object>> map, String theme) {
        final Map<String, Object> resourceMap = new LinkedHashMap<String, Object>();
        map.put("resourceMap", resourceMap);
        resourceMap.put("baseDir", path + "/src/main");
        resourceMap.put("resourceType", DfFreeGenResourceType.PROP.name());
        resourceMap.put("resourceFile", "$$baseDir$$/resources/" + appName + "_" + theme + ".properties");
    }

    protected Map<String, Object> createTableMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("isLastaFlute", true);
        return map;
    }

    // ===================================================================================
    //                                                                          Overridden
    //                                                                          ==========
    protected <VALUE> VALUE filterOverridden(VALUE overriddenValue, Map<String, Object> lastafluteMap, String appName, String title,
            String key) {
        @SuppressWarnings("unchecked")
        final Map<String, VALUE> overrideMap = (Map<String, VALUE>) lastafluteMap.get("overrideMap");
        if (overrideMap == null) {
            return overriddenValue;
        }
        final String fullKey = appName + ".freeGen." + title + "." + key;
        return (VALUE) overrideMap.getOrDefault(fullKey, overriddenValue);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String buildTitleSuffix(String theme) {
        return initCap(theme);
    }

    protected String getTrueLiteral() {
        return "true";
    }

    protected String initCap(String project) {
        return Srl.initCap(project);
    }
}
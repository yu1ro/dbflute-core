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
package org.dbflute.task.manage;

import java.io.File;

import org.dbflute.infra.dfprop.DfPublicProperties;
import org.dbflute.properties.DfInfraProperties;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.dbflute.task.bs.DfAbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
public class DfIntroTask extends DfAbstractTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfIntroTask.class);
    protected static final String DEFAULT_LOCATION_PATH = "../dbflute-intro.jar";

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|                   Intro                  |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.Intro);
        return true;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    @Override
    protected boolean isUseDataSource() {
        return false;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        final String locationPath = findLocationPath();
        final File jarFile = new File(locationPath);
        if (jarFile.exists()) { // basically no way because of script control
            _log.info("*Intro file already exists: " + locationPath);
            return;
        }
        final String downloadUrl = findDownloadUrl();
        _log.info("...Downloading DBFlute Intro to " + locationPath);
        _log.info("    from " + downloadUrl);
        download(downloadUrl, locationPath);
        refreshResources();
    }

    protected String findLocationPath() {
        final DfInfraProperties prop = getInfraProperties();
        final String specified = prop.getDBFluteIntroLocationPath();
        return specified != null ? specified : DEFAULT_LOCATION_PATH;
    }

    protected String findDownloadUrl() {
        final DfInfraProperties prop = getInfraProperties();
        final String specified = prop.getDBFluteIntroDownloadUrl();
        if (specified != null) {
            return specified;
        }
        final DfPublicProperties dfprop = preparePublicProperties();
        final String downloadUrl = dfprop.getIntroDownloadUrl();
        if (downloadUrl == null) {
            String msg = "Not found the download URL for DBFlute Intro in publicMap.";
            throw new IllegalStateException(msg);
        }
        return downloadUrl;
    }
}

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.sample.rxt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.generic.stub.beans.xsd.ContentArtifactsBean;
import org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient;
import org.wso2.carbon.governance.generic.ui.utils.DropDownDataPopulator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;


public class WSDLPopulator implements DropDownDataPopulator {

    private static final Log log = LogFactory.getLog(WSDLPopulator.class);
    String[] out = new String[1];

    @Override
    public String[] getList(HttpServletRequest httpServletRequest, ServletConfig servletConfig) {

        try {
            ManageGenericArtifactServiceClient client =
                    new ManageGenericArtifactServiceClient(servletConfig, httpServletRequest.getSession());

            ContentArtifactsBean artifactsBean = client.listContentArtifacts("application/wsdl+xml");
            if (artifactsBean.getName() != null && artifactsBean.getName().length > 0) {
                return artifactsBean.getPath();
                // To get name of the asset
                // return artifactsBean.getName();
            } else {
                out[0] = "None";
            }

        } catch (Exception e) {
            log.error("An error occurred while obtaining the WSDL list", e);
        }
        return out;
    }

    @Override
    public String[] getList(String s, String s1, Registry registry) {
        try {
            Registry sysRegistry = GovernanceUtils.getGovernanceSystemRegistry(registry);
            GenericArtifactManager manager = new GenericArtifactManager(sysRegistry, "wsdl");
            int count = manager.getAllGenericArtifacts().length;
            if(count>0) {
                String[] list = new String[count];
                for (int i = 0; i < count; i++) {
                    list[i] = manager.getAllGenericArtifacts()[i].getPath();
                    // To get name of the asset
                    // list[i] = manager.getAllGenericArtifacts()[i].getQName().getLocalPart();
                }
                return list;
            } else {
                out[0] = "None";
            }
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the WSDL list", e);
        }

        return out;
    }
}

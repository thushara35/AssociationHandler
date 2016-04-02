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

package org.wso2.carbon.registry.sample.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * This class will create associations between provided wsdl path and application resource path.
 */
public class AssociationHandler extends Handler {

    private static final Log log = LogFactory.getLog(AssociationHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {

        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            String resourceContent;
            String resourcePath = requestContext.getResourcePath().getPath();
            OMElement resourceElement;
            // Derive registry resource content
            Object resourceContentObj = requestContext.getResource().getContent();
            if (resourceContentObj instanceof String) {
                resourceContent = (String) resourceContentObj;
            } else {
                resourceContent = new String((byte[]) resourceContentObj);
            }
            try {
                // Initialize XMLInputFactory
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                // Reading xml content from string resourceContent and creating XMLStreamReader
                XMLStreamReader reader = inputFactory.
                        createXMLStreamReader(new StringReader(resourceContent));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                resourceElement = builder.getDocumentElement();
            } catch (XMLStreamException e) {
                String msg = "An error occurred " +
                        "while reading the resource at " + resourcePath + ".";
                throw new RegistryException(msg, e);
            }
            // Get the first OMElement child with name 'overview'
            OMElement elementOverview = getFirstChild(resourceElement, "overview");
            // Get the first OMElement child with name 'wsdl' and appending absolute path prefix.
            String wsdlPath = "/_system/governance" + getFirstChild(elementOverview, "wsdl").getText();
            Registry registry = requestContext.getRegistry();
            registry.put(resourcePath, requestContext.getResource());

            // Creating associations. wsdl DEPENDS on application
            registry.addAssociation(wsdlPath, requestContext.getResourcePath().getCompletePath(),
                    CommonConstants.DEPENDS);
            // Creating associations. application USED_BY wsdl
            registry.addAssociation(requestContext.getResourcePath().getCompletePath(), wsdlPath,
                    CommonConstants.USED_BY);


        } catch (RegistryException e) {
            String msg =
                    "Error occurred creating associations for " + requestContext.getResourcePath().getCompletePath();
            log.error(msg, e);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     *
     * @param element resource OMElement
     * @param elementName name of the element
     * @return OMElement
     */
    private OMElement getFirstChild(OMElement element, String elementName) {
        return element.getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, elementName));
    }

}
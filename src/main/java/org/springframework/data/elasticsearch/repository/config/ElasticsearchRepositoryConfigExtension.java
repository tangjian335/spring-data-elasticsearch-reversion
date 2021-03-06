/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.repository.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link org.springframework.data.repository.config.RepositoryConfigurationExtension} implementation to configure
 * Elasticsearch repository configuration support, evaluating the {@link EnableElasticsearchRepositories} annotation or
 * the equivalent XML element.
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 */
public class ElasticsearchRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtension#getRepositoryFactoryClassName()
     */
    @Override
    public String getRepositoryFactoryClassName() {
        return ElasticsearchRepositoryFactoryBean.class.getName();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getModulePrefix()
     */
    @Override
    protected String getModulePrefix() {
        return "elasticsearch";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

        AnnotationAttributes attributes = config.getAttributes();
        builder.addPropertyReference("elasticsearchOperations", attributes.getString("elasticsearchTemplateRef"));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.XmlRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {

        Element element = config.getElement();
        builder.addPropertyReference("elasticsearchOperations", element.getAttribute("elasticsearch-template-ref"));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingAnnotations()
     */
    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.<Class<? extends Annotation>>singleton(Document.class);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
     */
    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Arrays.<Class<?>>asList(ElasticsearchRepository.class, ElasticsearchCrudRepository.class);
    }
}

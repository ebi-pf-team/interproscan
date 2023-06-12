/*
 * Copyright 2009 the original author or authors.
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

/**
 * Provides data model classes for InterProScan.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */

@XmlSchema(elementFormDefault = XmlNsForm.QUALIFIED, namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlJavaTypeAdapter(value = DateAdapter.class, type = Date.class)
package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

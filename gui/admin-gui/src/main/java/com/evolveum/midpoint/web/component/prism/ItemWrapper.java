/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.component.prism;

import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Revivable;
import com.evolveum.midpoint.util.DebugDumpable;

/**
 * @author lazyman
 */
public interface ItemWrapper<I extends Item, ID extends ItemDefinition> extends Revivable, DebugDumpable {

	QName getName();
	
    String getDisplayName();

    void setDisplayName(String name);

    I getItem();
    
    ID getItemDefinition();
    
    boolean isReadonly();

	boolean isEmpty();

    boolean hasChanged();
    
    public List<ValueWrapper> getValues();
    
    public boolean isVisible();
    
    ContainerWrapper getContainer();
    
    public void addValue();

}

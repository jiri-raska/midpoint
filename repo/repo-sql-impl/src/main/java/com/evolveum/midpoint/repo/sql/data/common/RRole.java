/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.DtoTranslationException;
import com.evolveum.midpoint.xml.ns._public.common.common_1.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.RoleType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lazyman
 */
@Entity
@Table(name = "role")
@ForeignKey(name = "fk_role")
public class RRole extends RObject {

    private Set<RAssignment> assignments;

    @OneToMany(mappedBy = "owner")
    @ForeignKey(name = "none")
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public Set<RAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<RAssignment> assignments) {
        this.assignments = assignments;
    }

    public static void copyToJAXB(RRole repo, RoleType jaxb, PrismContext prismContext) throws
            DtoTranslationException {
        RObject.copyToJAXB(repo, jaxb, prismContext);

        if (repo.getAssignments() != null) {
            for (RAssignment rAssignment : repo.getAssignments()) {
                jaxb.getAssignment().add(rAssignment.toJAXB(prismContext));
            }
        }
    }

    public static void copyFromJAXB(RoleType jaxb, RRole repo, boolean pushCreateIdentificators,
            PrismContext prismContext) throws DtoTranslationException {
        RObject.copyFromJAXB(jaxb, repo, pushCreateIdentificators, prismContext);

        if (jaxb.getAssignment() != null && !jaxb.getAssignment().isEmpty()) {
            repo.setAssignments(new HashSet<RAssignment>());
        }
        long id = 1;
        for (AssignmentType assignment : jaxb.getAssignment()) {
            RAssignment rAssignment = new RAssignment();
            rAssignment.setOwner(repo);
            if (pushCreateIdentificators) {
                rAssignment.setOwnerOid(repo.getOid());
                rAssignment.setOwnerId(repo.getId());
                rAssignment.setOid(repo.getOid());
                rAssignment.setId(id);
                id++;
            }
            RAssignment.copyFromJAXB(assignment, rAssignment, pushCreateIdentificators, prismContext);

            repo.getAssignments().add(rAssignment);
        }
    }

    @Override
    public RoleType toJAXB(PrismContext prismContext) throws DtoTranslationException {
        RoleType object = new RoleType();
        RRole.copyToJAXB(this, object, prismContext);
        RUtil.revive(object.asPrismObject(), RoleType.class, prismContext);
        return object;
    }
}

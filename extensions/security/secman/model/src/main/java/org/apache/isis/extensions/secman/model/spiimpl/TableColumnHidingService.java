package org.apache.isis.extensions.secman.model.spiimpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.tablecol.TableColumnOrderService;
import org.apache.isis.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.model.dom.user.MeService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.secman.TableColumnHidingService")
@Order(OrderPrecedence.LATE - 10) // ensure comes before TableColumnOrderServiceDefault
@Qualifier("Secman")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TableColumnHidingService implements TableColumnVisibilityService {

    final MeService meService;
    final MetaModelService metaModelService;

    @Override
    public boolean hides(Class<?> collectionType, String memberId) {
        val me = meService.me();
        val permissionSet = me.getPermissionSet();
        val objectType = metaModelService.toObjectType(collectionType);
        val featureId = ApplicationFeatureId.newMember(objectType, memberId);
        val granted = permissionSet.evaluate(featureId, ApplicationPermissionMode.VIEWING).isGranted();
        return !granted;
    }

}
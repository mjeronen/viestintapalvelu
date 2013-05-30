package fi.vm.sade.viestintapalvelu.domain.jalkiohjauskirje;

import java.util.List;

import fi.vm.sade.viestintapalvelu.infrastructure.AbstractBatch;

public class JalkiohjauskirjeBatch extends AbstractBatch<Jalkiohjauskirje> {
	public JalkiohjauskirjeBatch() {
		super();
	}

	public JalkiohjauskirjeBatch(List<Jalkiohjauskirje> contents) {
		super(contents);
	}

	@Override
	protected AbstractBatch<Jalkiohjauskirje> createSubBatch(
			List<Jalkiohjauskirje> contentsOfSubBatch) {
		return new JalkiohjauskirjeBatch(contentsOfSubBatch);
	}
}
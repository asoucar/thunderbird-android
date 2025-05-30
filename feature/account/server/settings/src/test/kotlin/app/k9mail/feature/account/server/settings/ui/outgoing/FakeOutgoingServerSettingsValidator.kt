package app.k9mail.feature.account.server.settings.ui.outgoing

import net.thunderbird.core.common.domain.usecase.validation.ValidationResult

class FakeOutgoingServerSettingsValidator(
    private val serverAnswer: ValidationResult = ValidationResult.Success,
    private val portAnswer: ValidationResult = ValidationResult.Success,
    private val usernameAnswer: ValidationResult = ValidationResult.Success,
    private val passwordAnswer: ValidationResult = ValidationResult.Success,
) : OutgoingServerSettingsContract.Validator {
    override fun validateServer(server: String): ValidationResult = serverAnswer
    override fun validatePort(port: Long?): ValidationResult = portAnswer
    override fun validateUsername(username: String): ValidationResult = usernameAnswer
    override fun validatePassword(password: String): ValidationResult = passwordAnswer
}

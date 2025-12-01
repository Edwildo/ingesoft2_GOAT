import React from "react";
import { Input, InputProps } from "../../common/Input/Input";
import { validateEmail } from "../../../utils/validators";

export interface EmailInputProps extends Omit<InputProps, "type"> {
  value?: string;
  validateOnBlur?: boolean;
  onValidationChange?: (isValid: boolean) => void;
  onBlur?: (e: React.FocusEvent<HTMLInputElement>) => void;
}

export const EmailInput: React.FC<EmailInputProps> = ({
  validateOnBlur = true,
  onValidationChange,
  value,
  error: externalError,
  onBlur,
  ...inputProps
}) => {
  const [internalError, setInternalError] = React.useState<
    string | undefined
  >();

  const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    if (validateOnBlur && value) {
      const validation = validateEmail(value);
      if (!validation.valid) {
        setInternalError(validation.message);
        onValidationChange?.(false);
      } else {
        setInternalError(undefined);
        onValidationChange?.(true);
      }
    }

    onBlur?.(e);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (internalError) {
      setInternalError(undefined);
      onValidationChange?.(true);
    }

    inputProps.onChange?.(e);
  };

  const error = externalError || internalError;

  return (
    <Input
      {...inputProps}
      type="email"
      value={value}
      error={error}
      onChange={handleChange}
      onBlur={handleBlur}
      autoComplete="email"
    />
  );
};

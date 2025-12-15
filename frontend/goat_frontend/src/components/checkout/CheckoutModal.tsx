import React, { useState } from "react";
import { useCart } from "../../context/CartContext";
import { orderService, CreateOrderRequest, Order } from "../../api/order.service";
import { Button } from "../common/Button";
import { Input } from "../common/Input";
import { Alert } from "../common/Alert";
import { OrderConfirmation } from "./OrderConfirmation";
import styles from "./CheckoutModal.module.css";

export interface CheckoutModalProps {
  onClose: () => void;
}

export const CheckoutModal: React.FC<CheckoutModalProps> = ({ onClose }) => {
  const { cart, refresh } = useCart();
  const [step, setStep] = useState<"form" | "processing" | "success">("form");
  const [order, setOrder] = useState<Order | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    street: "",
    city: "",
    state: "",
    postalCode: "",
    country: "",
    shippingMethod: "STANDARD" as "STANDARD" | "EXPRESS",
  });

  const [formErrors, setFormErrors] = useState<Partial<typeof formData>>({});

  const total = cart?.items.reduce((sum, item) => sum + item.price, 0) || 0;

  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat("es-CO", {
      style: "currency",
      currency: "COP",
      minimumFractionDigits: 0,
    }).format(price);
  };

  const validateForm = (): boolean => {
    const errors: Partial<typeof formData> = {};

    if (!formData.street.trim()) {
      errors.street = "La calle es requerida";
    }

    if (!formData.city.trim()) {
      errors.city = "La ciudad es requerida";
    }

    if (!formData.state.trim()) {
      errors.state = "El estado/provincia es requerido";
    }

    if (!formData.postalCode.trim()) {
      errors.postalCode = "El código postal es requerido";
    }

    if (!formData.country.trim()) {
      errors.country = "El país es requerido";
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    if (!cart || cart.items.length === 0) {
      setError("El carrito está vacío");
      return;
    }

    setIsLoading(true);
    setError(null);
    setStep("processing");

    try {
      const request: CreateOrderRequest = {
        shippingAddress: {
          street: formData.street,
          city: formData.city,
          state: formData.state,
          postalCode: formData.postalCode,
          country: formData.country,
        },
        shippingMethod: formData.shippingMethod,
      };

      const response = await orderService.createOrder(request);

      if (response.success && response.data) {
        setOrder(response.data);
        setStep("success");
        await refresh(); // Actualizar carrito (debería estar vacío ahora)
      } else {
        setError(response.error?.message || "Error al procesar la orden");
        setStep("form");
      }
    } catch (err) {
      console.error(err);
      setError("Error de conexión con el servidor");
      setStep("form");
    } finally {
      setIsLoading(false);
    }
  };

  if (step === "processing") {
    return (
      <div className={styles.overlay} onClick={onClose}>
        <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
          <div className={styles.processing}>
            <div className={styles.spinner}></div>
            <h2>Procesando pago...</h2>
            <p>Por favor espera mientras procesamos tu pago</p>
          </div>
        </div>
      </div>
    );
  }

  if (step === "success" && order) {
    return (
      <OrderConfirmation order={order} onClose={onClose} />
    );
  }

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2>Checkout</h2>
          <button className={styles.closeButton} onClick={onClose} aria-label="Cerrar">
            ×
          </button>
        </div>

        {error && (
          <Alert variant="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.section}>
            <h3>Dirección de envío</h3>
            <div className={styles.formGrid}>
              <div className={styles.formGroup}>
                <label htmlFor="street">Calle *</label>
                <Input
                  id="street"
                  type="text"
                  value={formData.street}
                  onChange={(e) =>
                    setFormData({ ...formData, street: e.target.value })
                  }
                  error={formErrors.street}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="city">Ciudad *</label>
                <Input
                  id="city"
                  type="text"
                  value={formData.city}
                  onChange={(e) =>
                    setFormData({ ...formData, city: e.target.value })
                  }
                  error={formErrors.city}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="state">Estado/Provincia *</label>
                <Input
                  id="state"
                  type="text"
                  value={formData.state}
                  onChange={(e) =>
                    setFormData({ ...formData, state: e.target.value })
                  }
                  error={formErrors.state}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="postalCode">Código postal *</label>
                <Input
                  id="postalCode"
                  type="text"
                  value={formData.postalCode}
                  onChange={(e) =>
                    setFormData({ ...formData, postalCode: e.target.value })
                  }
                  error={formErrors.postalCode}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="country">País *</label>
                <Input
                  id="country"
                  type="text"
                  value={formData.country}
                  onChange={(e) =>
                    setFormData({ ...formData, country: e.target.value })
                  }
                  error={formErrors.country}
                  required
                />
              </div>
            </div>
          </div>

          <div className={styles.section}>
            <h3>Método de envío</h3>
            <div className={styles.shippingOptions}>
              <label className={styles.radioOption}>
                <input
                  type="radio"
                  name="shippingMethod"
                  value="STANDARD"
                  checked={formData.shippingMethod === "STANDARD"}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      shippingMethod: e.target.value as "STANDARD" | "EXPRESS",
                    })
                  }
                />
                <div>
                  <strong>Estándar</strong>
                  <span>5-7 días hábiles</span>
                </div>
              </label>

              <label className={styles.radioOption}>
                <input
                  type="radio"
                  name="shippingMethod"
                  value="EXPRESS"
                  checked={formData.shippingMethod === "EXPRESS"}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      shippingMethod: e.target.value as "STANDARD" | "EXPRESS",
                    })
                  }
                />
                <div>
                  <strong>Express</strong>
                  <span>2-3 días hábiles</span>
                </div>
              </label>
            </div>
          </div>

          <div className={styles.summary}>
            <h3>Resumen</h3>
            <div className={styles.summaryRow}>
              <span>Items ({cart?.items.length || 0})</span>
              <span>{formatPrice(total)}</span>
            </div>
            <div className={styles.summaryRow}>
              <strong>Total</strong>
              <strong>{formatPrice(total)}</strong>
            </div>
          </div>

          <div className={styles.actions}>
            <Button type="button" variant="outline" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" variant="primary" isLoading={isLoading} fullWidth>
              Confirmar compra
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};


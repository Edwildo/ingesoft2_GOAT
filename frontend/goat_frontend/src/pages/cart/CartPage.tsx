import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../../context/CartContext";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { Loading } from "../../components/common/Loading";
import { Alert } from "../../components/common/Alert";
import styles from "./CartPage.module.css";

export const CartPage: React.FC = () => {
  const navigate = useNavigate();
  const { cart, isLoading, error, removeItem, refresh } = useCart();
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!cart) {
      void refresh();
    }
  }, [cart, refresh]);

  const total = useMemo(() => {
    if (!cart) return 0;
    return cart.items.reduce((sum, item) => sum + item.price, 0);
  }, [cart]);

  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat("es-CO", {
      style: "currency",
      currency: "COP",
      minimumFractionDigits: 0,
    }).format(price);
  };

  const handleRemove = async (itemId: string) => {
    const result = await removeItem(itemId);
    if (!result.success && result.message) {
      setMessage(result.message);
    }
  };

  if (isLoading && !cart) {
    return (
      <div className={styles.cartPage}>
        <div className={styles.container}>
          <Loading message="Cargando carrito..." />
        </div>
      </div>
    );
  }

  return (
    <div className={styles.cartPage}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h1 className={styles.title}>Tu carrito</h1>
          <div className={styles.actions}>
            <Button variant="outline" onClick={() => navigate("/shop")}>
              Seguir comprando
            </Button>
          </div>
        </div>

        {error && (
          <Alert variant="error" onClose={() => void refresh()}>
            {error}
          </Alert>
        )}

        {message && (
          <Alert variant="error" onClose={() => setMessage(null)}>
            {message}
          </Alert>
        )}

        {!cart || cart.items.length === 0 ? (
          <div className={styles.empty}>
            <p>Tu carrito está vacío.</p>
            <Button variant="primary" onClick={() => navigate("/shop")}>
              Ir al shop
            </Button>
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "1fr auto", gap: "var(--theme-spacing-xl)" }}>
            <div className={styles.list}>
              {cart.items.map((item) => (
                <div key={item.id} className={styles.itemCard}>
                  {item.coverImage ? (
                    <img
                      src={item.coverImage}
                      alt={item.brand}
                      className={styles.cover}
                      onError={(e) => {
                        (e.target as HTMLImageElement).src =
                          "/placeholder-sneaker.png";
                      }}
                    />
                  ) : (
                    <div className={styles.placeholder}>Sin imagen</div>
                  )}

                  <div className={styles.meta}>
                    <div>
                      <strong>{item.brand}</strong>
                    </div>
                    <div>Talla: {item.size}</div>
                    <div>Color: {item.color}</div>
                    <div className={styles.price}>{formatPrice(item.price)}</div>
                  </div>

                  <Button
                    variant="outline"
                    size="small"
                    onClick={() => handleRemove(item.id)}
                  >
                    Eliminar
                  </Button>
                </div>
              ))}
            </div>

            <Card className={styles.summary}>
              <h3>Resumen</h3>
              <p>
                Total ({cart.items.length} items): <strong>{formatPrice(total)}</strong>
              </p>
              <Button variant="primary" disabled fullWidth>
                Checkout (Próximamente)
              </Button>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
};

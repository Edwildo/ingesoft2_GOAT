import React, { useState } from "react";
import { catalogService } from "../../../api/catalog.service";
import { Card } from "../../common/Card";
import { Button } from "../../common/Button";
import { Input } from "../../common/Input";
import { Alert } from "../../common/Alert";
import { CreateSneakerRequest } from "../../../types/catalog.types";
import styles from "./CreateSneakerModal.module.css";

export interface CreateSneakerModalProps {
  sku: string;
  onClose: () => void;
  onSuccess: (sneaker: CreateSneakerRequest) => void;
}

export const CreateSneakerModal: React.FC<CreateSneakerModalProps> = ({
  sku,
  onClose,
  onSuccess,
}) => {
  const [formData, setFormData] = useState<CreateSneakerRequest>({
    sku: sku.toUpperCase(),
    brand: "",
    model: "",
    gender: "UNISEX",
    description: "",
    categories: [],
    collections: [],
    media: {
      coverImage: "",
      gallery: [],
    },
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateSneakerRequest, string>>
  >({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof CreateSneakerRequest, string>> = {};

    if (!formData.sku || formData.sku.length < 3 || formData.sku.length > 50) {
      newErrors.sku = "El SKU debe tener entre 3 y 50 caracteres";
    } else if (!/^[A-Z0-9_-]+$/.test(formData.sku)) {
      newErrors.sku =
        "El SKU solo puede contener letras mayúsculas, números, guiones y guiones bajos";
    }

    if (!formData.brand) {
      newErrors.brand = "La marca es requerida";
    }

    if (!formData.model) {
      newErrors.model = "El modelo es requerido";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    setMessage(null);

    try {
      const response = await catalogService.createSneaker(formData);

      if (response.success && response.data) {
        setMessage({
          type: "success",
          text: "Sneaker creado exitosamente en el catálogo",
        });
        setTimeout(() => {
          onSuccess(formData);
          onClose();
        }, 1000);
      } else {
        setMessage({
          type: "error",
          text:
            response.error?.message || "Error al crear sneaker en el catálogo",
        });
      }
    } catch (err) {
      console.error(err);
      setMessage({ type: "error", text: "Error de conexión con el servidor" });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <Card className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2 className={styles.title}>Crear Sneaker en Catálogo</h2>
          <button
            className={styles.closeButton}
            onClick={onClose}
            type="button"
          >
            ×
          </button>
        </div>

        {message && (
          <Alert
            variant={message.type === "error" ? "error" : "success"}
            onClose={() => setMessage(null)}
          >
            {message.text}
          </Alert>
        )}

        <form onSubmit={handleSubmit} className={styles.form}>
          <p className={styles.description}>
            El SKU <strong>{sku}</strong> no existe en el catálogo. Por favor,
            completa la información para crearlo.
          </p>

          <Input
            id="sku"
            label="SKU"
            value={formData.sku}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                sku: e.target.value.toUpperCase(),
              }))
            }
            error={errors.sku}
            required
            fullWidth
            disabled={isLoading}
            helperText="Solo mayúsculas, números, guiones (-) y guiones bajos (_)"
          />

          <div className={styles.row}>
            <Input
              id="brand"
              label="Marca"
              value={formData.brand}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, brand: e.target.value }))
              }
              error={errors.brand}
              required
              fullWidth
              disabled={isLoading}
            />

            <Input
              id="model"
              label="Modelo"
              value={formData.model}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, model: e.target.value }))
              }
              error={errors.model}
              required
              fullWidth
              disabled={isLoading}
            />
          </div>

          <div className={styles.selectGroup}>
            <label htmlFor="gender" className={styles.label}>
              Género <span className={styles.required}>*</span>
            </label>
            <select
              id="gender"
              value={formData.gender}
              onChange={(e) =>
                setFormData((prev) => ({
                  ...prev,
                  gender: e.target.value as "MALE" | "FEMALE" | "UNISEX",
                }))
              }
              className={styles.select}
              required
              disabled={isLoading}
            >
              <option value="MALE">Masculino</option>
              <option value="FEMALE">Femenino</option>
              <option value="UNISEX">Unisex</option>
            </select>
          </div>

          <Input
            id="description"
            label="Descripción"
            value={formData.description || ""}
            onChange={(e) =>
              setFormData((prev) => ({ ...prev, description: e.target.value }))
            }
            fullWidth
            disabled={isLoading}
            helperText="Opcional"
          />

          <Input
            id="coverImage"
            label="URL de Imagen Principal"
            type="url"
            value={formData.media?.coverImage || ""}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                media: { ...prev.media, coverImage: e.target.value },
              }))
            }
            fullWidth
            disabled={isLoading}
            helperText="Opcional"
          />

          <div className={styles.actions}>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isLoading}
            >
              Cancelar
            </Button>
            <Button type="submit" variant="primary" isLoading={isLoading}>
              Crear Sneaker
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
